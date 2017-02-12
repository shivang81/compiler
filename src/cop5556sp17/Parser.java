package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

import java.util.HashSet;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

    HashSet<Kind> firstOfDec;
    HashSet<Kind> firstOfStatement;
    HashSet<Kind> firstOfParamDec;
    HashSet<Kind> filterOps;
    HashSet<Kind> frameOps;
    HashSet<Kind> imageOps;
    HashSet<Kind> firstOfChainElem;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
        this.initializeFirstSets();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	void parse() throws SyntaxException {
		program();
		matchEOF();
		return;
	}

	void expression() throws SyntaxException {
		term();
		while (t.kind.equals(LT) || t.kind.equals(LE) || t.kind.equals(GT) || t.kind.equals(GE)
                || t.kind.equals(EQUAL) || t.kind.equals(NOTEQUAL)) {
            consume();
			term();
		}
	}

	void term() throws SyntaxException {
		elem();
		while (t.kind.equals(PLUS) || t.kind.equals(MINUS) || t.kind.equals(OR)) {
			consume();
			elem();
		}
	}

	void elem() throws SyntaxException {
		factor();
        while (t.kind.equals(TIMES) || t.kind.equals(DIV) || t.kind.equals(AND) || t.kind.equals(MOD)) {
            consume();
            factor();
        }
	}

	void factor() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			consume();
		}
			break;
		case INT_LIT: {
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			consume();
		}
			break;
		case LPAREN: {
			consume();
			expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
	}

	void block() throws SyntaxException {
        match(LBRACE);
        while (firstOfDec.contains(t.kind) || firstOfStatement.contains(t.kind)) {
            if(firstOfDec.contains(t.kind)) {
                dec();
            } else if(firstOfStatement.contains(t.kind)) {
                statement();
            } else {
                throw new SyntaxException("illegal block");
            }
        }
        match(RBRACE);
	}

	void program() throws SyntaxException {
		match(IDENT);
        Kind kind = t.kind;
        if(kind.equals(LBRACE)) {
            block();
        } else if(firstOfParamDec.contains(kind)) {
            paramDec();
            while(t.kind.equals(COMMA)) {
                consume();
                paramDec();
            }
            block();
        } else {
            throw new SyntaxException("illegal program");
        }
	}

	void paramDec() throws SyntaxException {
		if(firstOfParamDec.contains(t.kind)) {
            consume();
            match(IDENT);
        } else {
            throw new SyntaxException("illegal param_dec");
        }
	}

	void dec() throws SyntaxException {
		if(firstOfDec.contains(t.kind)) {
            consume();
            match(IDENT);
        } else {
            throw new SyntaxException("illegal dec");
        }
	}

	void statement() throws SyntaxException {
        if(firstOfStatement.contains(t.kind)) {
            if(t.kind.equals(OP_SLEEP)) {
                consume();
                expression();
                match(SEMI);
            } else if(t.kind.equals(KW_WHILE)) {
                whileStatement();
            } else if(t.kind.equals(KW_IF)) {
                ifStatement();
            } else if(t.kind.equals(IDENT)) {
                if(scanner.peek().kind.equals(ASSIGN)) {
                    consume();
                    match(ASSIGN);
                    expression();
                    match(SEMI);
                } else if(scanner.peek().kind.equals(ARROW) || scanner.peek().kind.equals(BARARROW)) {
                    chain();
                    match(SEMI);
                } else {
                    throw new SyntaxException("illegal statement");
                }

            } else {
                chain();
                match(SEMI);
            }
        } else {
            throw new SyntaxException("illegal statement");
        }
    }

	void chain() throws SyntaxException {
        if(firstOfChainElem.contains(t.kind)) {
            chainElem();
            arrowOp();
            chainElem();
            while (t.kind.equals(ARROW) || t.kind.equals(BARARROW)) {
                consume();
                chainElem();
            }
        } else {
            throw new SyntaxException("illegal chain expression");
        }
	}

    void arrowOp() throws SyntaxException {
        Kind kind = t.kind;
        if(kind.equals(ARROW) || kind.equals(BARARROW)) {
            consume();
        }
        else{
            throw new SyntaxException("illegal arrowOp");
        }
    }

	void chainElem() throws SyntaxException {
        if(firstOfChainElem.contains(t.kind)) {
            if(t.kind.equals(IDENT)) {
                consume();
            } else if(imageOps.contains(t.kind) || frameOps.contains(t.kind) || filterOps.contains(t.kind)) {
                consume();
                arg();
            } else {
                throw new SyntaxException("illegal chainElem");
            }
        }
	}

	void arg() throws SyntaxException {
        if(t.kind.equals(LPAREN)) {
            consume();
            expression();
            while(t.kind.equals(COMMA)) {
                consume();
                expression();
            }
            match(RPAREN);
        } else {
            return;
        }
	}

    void ifStatement() throws SyntaxException {
        if(t.kind.equals(KW_IF)) {
            consume();
            match(LPAREN);
            expression();
            match(RPAREN);
            block();
        } else {
            throw new SyntaxException("illegal ifStatement");
        }
    }

    void whileStatement() throws SyntaxException {
        if(t.kind.equals(KW_WHILE)) {
            consume();
            match(LPAREN);
            expression();
            match(RPAREN);
            block();
        } else {
            throw new SyntaxException("illegal ifStatement");
        }
    }

    void assign() throws SyntaxException {
        match(IDENT);
        match(ASSIGN);
        expression();
    }

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind.equals(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind.equals(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + " expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		for(Kind kind : kinds) {
            if (t.kind.equals(kind)) {
                return consume();
            }
        }
        throw new SyntaxException("kinds did not match");
    }

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

    private void initializeFirstSets() {
        initializeFirstOfDec();
        initializeFirstOfStatement();
        initializeFirstOfParamDec();
        initializeFilterOps();
        initializeFrameOps();
        initializeImageOps();
        initializeFirstOfChainElem();

    }

    private void initializeFirstOfDec() {
        firstOfDec = new HashSet<>();
        firstOfDec.add(KW_INTEGER);
        firstOfDec.add(KW_BOOLEAN);
        firstOfDec.add(KW_IMAGE);
        firstOfDec.add(KW_FRAME);
    }

    private void initializeFirstOfStatement() {
        firstOfStatement = new HashSet<>();
        firstOfStatement.add(OP_SLEEP);
        firstOfStatement.add(KW_WHILE);
        firstOfStatement.add(KW_IF);
        firstOfStatement.add(IDENT);
        firstOfStatement.add(OP_BLUR);
        firstOfStatement.add(OP_GRAY);
        firstOfStatement.add(OP_CONVOLVE);
        firstOfStatement.add(KW_SHOW);
        firstOfStatement.add(KW_HIDE);
        firstOfStatement.add(KW_MOVE);
        firstOfStatement.add(KW_XLOC);
        firstOfStatement.add(KW_YLOC);
        firstOfStatement.add(OP_WIDTH);
        firstOfStatement.add(OP_HEIGHT);
        firstOfStatement.add(KW_SCALE);
    }

    private void initializeFirstOfParamDec() {
        firstOfParamDec = new HashSet<>();
        firstOfParamDec.add(KW_URL);
        firstOfParamDec.add(KW_FILE);
        firstOfParamDec.add(KW_INTEGER);
        firstOfParamDec.add(KW_BOOLEAN);
    }

    private void initializeFilterOps() {
        filterOps = new HashSet<>();
        filterOps.add(OP_BLUR);
        filterOps.add(OP_GRAY);
        filterOps.add(OP_CONVOLVE);
    }

    private void initializeFrameOps() {
        frameOps = new HashSet<>();
        frameOps.add(KW_SHOW);
        frameOps.add(KW_HIDE);
        frameOps.add(KW_MOVE);
        frameOps.add(KW_XLOC);
        frameOps.add(KW_YLOC);
    }

    private void initializeImageOps() {
        imageOps = new HashSet<>();
        imageOps.add(OP_WIDTH);
        imageOps.add(OP_HEIGHT);
        imageOps.add(KW_SCALE);
    }

    private void initializeFirstOfChainElem() {
        firstOfChainElem = new HashSet<>();
        firstOfChainElem.add(IDENT);
        firstOfChainElem.addAll(filterOps);
        firstOfChainElem.addAll(frameOps);
        firstOfChainElem.addAll(imageOps);
    }

}
