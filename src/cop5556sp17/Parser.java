package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Parser{

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
    Program parse() throws SyntaxException {
        Program p = program();
        matchEOF();
        return p;
    }

    Expression expression() throws SyntaxException {
        Expression e0 = null;
        Expression e1 = null;
        Token firstToken = t;
        e0 = term();
        while (t.kind.equals(LT) || t.kind.equals(LE) || t.kind.equals(GT) || t.kind.equals(GE)
                || t.kind.equals(EQUAL) || t.kind.equals(NOTEQUAL)) {
            Token op = t;
            consume();
            e1 = term();
            e0 = new BinaryExpression(firstToken,e0,op,e1);
        }
        return e0;
    }

    Expression term() throws SyntaxException {
        Expression e0 = null;
        Expression e1 = null;
        Token firstToken = t;
        e0 = elem();
        while (t.kind.equals(PLUS) || t.kind.equals(MINUS) || t.kind.equals(OR)) {
            Token op = t;
            consume();
            e1 = elem();
            e0 = new BinaryExpression(firstToken,e0,op,e1);

        }
        return e0;
    }

    Expression elem() throws SyntaxException {
        Expression e0 = null;
        Expression e1 = null;
        Token firstToken = t;
        e0 = factor();
        while (t.kind.equals(TIMES) || t.kind.equals(DIV) || t.kind.equals(AND) || t.kind.equals(MOD)) {
            Token op = t;
            consume();
            e1 = factor();
            e0 = new BinaryExpression(firstToken,e0,op,e1);
        }
        return e0;
    }

    Expression factor() throws SyntaxException {
        Expression e = null;
        Kind kind = t.kind;
        switch (kind) {
            case IDENT: {
                e = new IdentExpression(t);
                consume();
            }
            break;
            case INT_LIT: {
                e = new IntLitExpression(t);
                consume();
            }
            break;
            case KW_TRUE:
            case KW_FALSE: {
                e = new BooleanLitExpression(t);
                consume();
            }
            break;
            case KW_SCREENWIDTH:
            case KW_SCREENHEIGHT: {
                e = new ConstantExpression(t);
                consume();
            }
            break;
            case LPAREN: {
                consume();
                e = expression();
                match(RPAREN);
            }
            break;
            default:
                //you will want to provide a more useful error message
                throw new SyntaxException("illegal factor");
        }
        return e;
    }

    Block block() throws SyntaxException {
        Block b0 = null;
        ArrayList<Dec> decList = new ArrayList<>();
        ArrayList<Statement> statementList = new ArrayList<>();
        Token firstToken = t;

        match(LBRACE);
        while (firstOfDec.contains(t.kind) || firstOfStatement.contains(t.kind)) {
            if(firstOfDec.contains(t.kind)) {
                Dec d = dec();
                decList.add(d);
            } else if(firstOfStatement.contains(t.kind)) {
                Statement s = statement();
                statementList.add(s);
            } else {
                throw new SyntaxException("illegal block");
            }
        }
        match(RBRACE);
        return new Block(firstToken,decList,statementList);
    }

    Program program() throws SyntaxException {
        ArrayList<ParamDec> paramList = new ArrayList<ParamDec>();
        Block b = null;
        Token firstToken = null;

        if(t.isKind(IDENT))
            firstToken = t;

        match(IDENT);
        Kind kind = t.kind;
        if(kind.equals(LBRACE)) {
            b = block();
        } else if(firstOfParamDec.contains(kind)) {
            paramList.add(paramDec());
            while(t.kind.equals(COMMA)) {
                consume();
                paramList.add(paramDec());
            }
            b = block();
        } else {
            throw new SyntaxException("illegal program");
        }
        return new Program(firstToken,paramList,b);
    }

    ParamDec paramDec() throws SyntaxException {
        Token firstToken = null;
        Token ident = null;

        if(firstOfParamDec.contains(t.kind)) {
            firstToken = t;
            consume();
            ident = t;
            match(IDENT);
        } else {
            throw new SyntaxException("illegal param_dec");
        }
        return new ParamDec(firstToken,ident);
    }

    Dec dec() throws SyntaxException {
        Token firstToken, ident;

        if(firstOfDec.contains(t.kind)) {
            firstToken = t;
            consume();
            ident = t;
            match(IDENT);
        } else {
            throw new SyntaxException("illegal dec");
        }
        return new Dec(firstToken,ident);
    }

    Statement statement() throws SyntaxException {
        Statement stmt = null;
        Token firstToken = null;

        if(firstOfStatement.contains(t.kind)) {
            if(t.kind.equals(OP_SLEEP)) {
                firstToken = t;
                consume();
                Expression e = expression();
                stmt = new SleepStatement(firstToken,e);
                match(SEMI);
            } else if(t.kind.equals(KW_WHILE)) {
                stmt = whileStatement();
            } else if(t.kind.equals(KW_IF)) {
                stmt = ifStatement();
            } else if(t.kind.equals(IDENT)) {
                firstToken = t;
                IdentLValue identLValue = new IdentLValue(t);
                if(scanner.peek().kind.equals(ASSIGN)) {
                    consume();
                    match(ASSIGN);
                    Expression e = expression();
                    match(SEMI);
                    stmt = new AssignmentStatement(firstToken,identLValue,e);
                } else if(scanner.peek().kind.equals(ARROW) || scanner.peek().kind.equals(BARARROW)) {
                    stmt = chain();
                    match(SEMI);
                } else {
                    throw new SyntaxException("illegal statement");
                }

            } else {
                stmt = chain();
                match(SEMI);
            }
        } else {
            throw new SyntaxException("illegal statement");
        }
        return stmt;
    }

    Chain chain() throws SyntaxException {
        Chain chain0;
        ChainElem chain1;
        chain0 = chainElem();
        Token op = arrowOp();
        chain1 = chainElem();
        chain0 = new BinaryChain(chain0.firstToken,chain0,op,chain1);
        while (t.isKind(ARROW) || t.isKind(BARARROW)){
            op = arrowOp();
            chain1 = chainElem();
            chain0 = new BinaryChain(chain0.firstToken,chain0,op,chain1);
        }
        return chain0;
    }

    ChainElem chainElem() throws SyntaxException {
        ChainElem chainElem = null;
        Token firstToken = null;

        if(t.isKind(IDENT)){
            firstToken = t;
            consume();
            chainElem = new IdentChain(firstToken);
        }else if(filterOps.contains(t.kind)){
            firstToken = t;
            consume();
            Tuple tuple = arg();
            chainElem = new FilterOpChain(firstToken,tuple);
        }else if(frameOps.contains(t.kind)){
            firstToken = t;
            consume();
            Tuple tuple = arg();
            chainElem = new FrameOpChain(firstToken,tuple);
        }else if(imageOps.contains(t.kind)){
            firstToken = t;
            consume();
            Tuple tuple = arg();
            chainElem = new ImageOpChain(firstToken,tuple);
        }else {
            throw new SyntaxException("Illegal chainElem");
        }
        return chainElem;
    }

    Token arrowOp() throws SyntaxException{
        Token token;

        if(t.isKind(ARROW) || t.isKind(BARARROW)){
            token = t;
            consume();
        }else {
            throw new SyntaxException("Illegal arrowOp");
        }
        return token;
    }

    Tuple arg() throws SyntaxException {
        Tuple tuple = null;
        List<Expression> expressionList  = new ArrayList<>();
        Token firstToken = null;

        if(t.kind.equals(LPAREN)) {
            firstToken = t;
            consume();
            expressionList.add(expression());
            while(t.kind.equals(COMMA)) {
                consume();
                expressionList.add(expression());
            }
            match(RPAREN);
            tuple = new Tuple(firstToken,expressionList);
            return tuple;
        }else {
            return new Tuple(t,expressionList);
        }
    }

    IfStatement ifStatement() throws SyntaxException {
        if(t.kind.equals(KW_IF)) {
            Token firstToken = t;
            consume();
            match(LPAREN);
            Expression expr = expression();
            match(RPAREN);
            Block b = block();
            return new IfStatement(firstToken,expr,b);
        } else {
            throw new SyntaxException("illegal ifStatement");
        }
    }

    WhileStatement whileStatement() throws SyntaxException {
        if(t.kind.equals(KW_WHILE)) {
            Token firstToken = t;
            consume();
            match(LPAREN);
            Expression expr = expression();
            match(RPAREN);
            Block b = block();
            return new WhileStatement(firstToken,expr,b);
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
