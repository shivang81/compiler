package cop5556sp17;

import cop5556sp17.AST.Expression;
import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cop5556sp17.AST.*;

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
    HashMap<String,HashSet<Kind>> firstSet = new HashMap<>();

    Parser(Scanner scanner) {
        this.scanner = scanner;
        t = scanner.nextToken();
        initializeFirstSet();
    }

    /**
     * parse the input using tokens from the scanner.
     * Check for EOF (i.e. no trailing junk) when finished
     *
     * @throws SyntaxException
     */

    ASTNode parse() throws SyntaxException {
        Program p = program();
        matchEOF();
        return p;
    }

    Expression expression() throws SyntaxException {
        //TODO
        Expression e0 = null;
        Expression e1 = null;
        Token firstToken = t;

        e0 = term();
        while(firstSet.get("relOp").contains(t.kind)){
            Token op = t;
            consume();
            e1 = term();
            e0 = new BinaryExpression(firstToken,e0,op,e1);
        }
        return e0;
    }

    Expression term() throws SyntaxException {
        //TODO
        Expression e0 = null;
        Expression e1 = null;
        Token firstToken = t;
        e0 = elem();
        while(firstSet.get("weakOp").contains(t.kind)){
            Token op = t;
            consume();
            e1 = elem();
            e0 = new BinaryExpression(firstToken,e0,op,e1);
        }
        return e0;
    }

    Expression elem() throws SyntaxException {
        //TODO
        Expression e0 = null;
        Expression e1 = null;
        Token firstToken = t;
        e0 = factor();

        while (firstSet.get("strongOp").contains(t.kind)){
            Token op = t;
            consume();
            e1 = factor();
            e0 = new BinaryExpression(firstToken,e0,op,e1);
        }
        return e0;
    }

    Expression factor() throws SyntaxException {
        Kind kind = t.kind;
        Expression e = null;

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
                throw new SyntaxException("Illegal factor");
        }
        return e;
    }

    Block block() throws SyntaxException {
        //TODO
        Block b0 = null;
        ArrayList<Dec> decList = new ArrayList<>();
        ArrayList<Statement> stmtList = new ArrayList<>();
        Token firstToken = t;

        match(LBRACE);
        while(firstSet.get("dec").contains(t.kind) || firstSet.get("statement").contains(t.kind)) {
            if (firstSet.get("dec").contains(t.kind)) {
                Dec d = dec();
                decList.add(d);
            } else if (firstSet.get("statement").contains(t.kind)) {
                Statement s = statement();
                stmtList.add(s);
            }
        }
        match(RBRACE);
        b0 = new Block(firstToken,decList,stmtList);
        return b0;
    }

    Program program() throws SyntaxException {
        //TODO
        Program p = null;
        ArrayList<ParamDec> paramList = new ArrayList<ParamDec>();
        Block b = null;
        Token firstToken = null;

        if(t.isKind(IDENT))
            firstToken = t;

        match(IDENT);
        if(t.isKind(LBRACE)){
            b = block();
        }else if (firstSet.get("paramDec").contains(t.kind)){
            paramList.add(paramDec());
            while (t.isKind(COMMA)){
                consume();
                paramList.add(paramDec());
            }
            b = block();
        }else {
            throw new SyntaxException("Illegal program");
        }
        p = new Program(firstToken,paramList,b);
        return p;
    }

    ParamDec paramDec() throws SyntaxException {
        //TODO
        ParamDec paramDec = null;
        Token firstToken = null;
        Token ident = null;

        if(firstSet.get("paramDec").contains(t.kind)){
            firstToken = t;
            consume();
            ident = t;
            match(IDENT);
        }else {
            throw new SyntaxException("Illegal paramDec");
        }
        paramDec = new ParamDec(firstToken,ident);
        return paramDec;
    }

    Dec dec() throws SyntaxException {
        //TODO
        Token firstToken, ident;

        if(firstSet.get("dec").contains(t.kind)){
            firstToken = t;
            consume();
            ident = t;
            match(IDENT);
        }else {
            throw new SyntaxException("Illegal dec");
        }
        return new Dec(firstToken,ident);
    }

    Statement statement() throws SyntaxException {
        //TODO
        Statement statement = null;
        Token firstToken = null;

        if(t.isKind(OP_SLEEP)){
            firstToken = t;
            consume();
            Expression e = expression();
            statement = new SleepStatement(firstToken,e);
            match(SEMI);
        }else if(t.isKind(KW_WHILE)){
            statement = whileStatement();
        }else if(t.isKind(KW_IF)){
            statement = ifStatement();
        }else if(t.isKind(IDENT)){
            Token nextToken = scanner.peek();
            if(nextToken.isKind(ASSIGN)){
                statement = assign();
            }else if(nextToken.isKind(ARROW) || nextToken.isKind(BARARROW)){
                statement = chain();
            }else {
                throw new SyntaxException("Illegal statement at "+t.getLinePos());
            }
            match(SEMI);
        }else if(firstSet.get("chainElem").contains(t.kind)){
            statement = chain();
            match(SEMI);
        }else {
            throw new SyntaxException("Illegal statement");
        }
        return statement;
    }

    Chain chain() throws SyntaxException {
        //TODO
        Chain c0;
        ChainElem c1;
        c0 = chainElem();
        Token arrow = arrowOp();
        c1 = chainElem();
        c0 = new BinaryChain(c0.firstToken,c0,arrow,c1);
        while (t.isKind(ARROW) || t.isKind(BARARROW)){
            arrow=arrowOp();
//            consume();
            c1 = chainElem();
            c0 = new BinaryChain(c0.firstToken,c0,arrow,c1);
        }
        return c0;
    }

    ChainElem chainElem() throws SyntaxException {
        //TODO
        ChainElem chainElem = null;
        Token firstToken = null;

        if(t.isKind(IDENT)){
            firstToken = t;
            consume();
            chainElem = new IdentChain(firstToken);
        }else if(firstSet.get("filterOp").contains(t.kind)){
            firstToken = t;
            consume();
            Tuple tuple = arg();
            chainElem = new FilterOpChain(firstToken,tuple);
        }else if(firstSet.get("frameOp").contains(t.kind)){
            firstToken = t;
            consume();
            Tuple tuple = arg();
            chainElem = new FrameOpChain(firstToken,tuple);
        }else if(firstSet.get("imageOp").contains(t.kind)){
            firstToken = t;
            consume();
            Tuple tuple = arg();
            chainElem = new ImageOpChain(firstToken,tuple);
        }else {
            throw new SyntaxException("Illegal chainElem");
        }
        return chainElem;
    }

    Tuple arg() throws SyntaxException {
        //TODO
        List<Expression> exprList = new ArrayList<>();
        Token firstToken = null;

        if(t.isKind(LPAREN)){
            firstToken = t;
            consume();
            exprList.add(expression());
            while (t.isKind(COMMA)){
                consume();
                exprList.add(expression());
            }
            match(RPAREN);
            return new Tuple(firstToken,exprList);
        }else {
            return new Tuple(t,exprList);
        }
    }

    WhileStatement whileStatement() throws SyntaxException{
        Token firstToken = t;
        match(KW_WHILE);
        match(LPAREN);
        Expression e = expression();
        match(RPAREN);
        Block b = block();
        return new WhileStatement(firstToken,e,b);
    }

    IfStatement ifStatement() throws SyntaxException{
        Token firstToken = t;
        match(KW_IF);
        match(LPAREN);
        Expression e = expression();
        match(RPAREN);
        Block b = block();
        return new IfStatement(firstToken,e,b);
    }

    AssignmentStatement assign() throws SyntaxException{
        Token firstToken = t;
        match(IDENT);
        IdentLValue identLValue = new IdentLValue(firstToken);
        match(ASSIGN);
        Expression e = expression();
        return new AssignmentStatement(firstToken,identLValue,e);
    }

    Token arrowOp() throws SyntaxException{
        Token token = null;
        if(t.isKind(ARROW) || t.isKind(BARARROW)){
            token = t;
            consume();
        }else {
            throw new SyntaxException("Illegal arrowOp");
        }
        return token;
    }

    /**
     * Checks whether the current token is the EOF token. If not, a
     * SyntaxException is thrown.
     *
     * @return
     * @throws SyntaxException
     */
    private Token matchEOF() throws SyntaxException {
        if (t.isKind(EOF)) {
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
        if (t.isKind(kind)) {

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
        // TODO. Optional but handy
        return null; //replace this statement
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

    void initializeFirstSet(){
        // block
        HashSet<Kind> block = new HashSet<>();
        block.add(Kind.LBRACE);
        firstSet.put("block",block);

        // paramdec
        HashSet<Kind> paramdec = new HashSet<>();
        paramdec.add(Kind.KW_URL);
        paramdec.add(Kind.KW_FILE);
        paramdec.add(Kind.KW_INTEGER);
        paramdec.add(Kind.KW_BOOLEAN);
        firstSet.put("paramDec",paramdec);

        // dec
        HashSet<Kind> dec = new HashSet<>();
        dec.add(Kind.KW_INTEGER);
        dec.add(Kind.KW_BOOLEAN);
        dec.add(Kind.KW_IMAGE);
        dec.add(Kind.KW_FRAME);
        firstSet.put("dec",dec);


        //filterOp
        HashSet<Kind> filterOp = new HashSet<>();
        filterOp.add(Kind.OP_BLUR);
        filterOp.add(Kind.OP_GRAY);
        filterOp.add(Kind.OP_CONVOLVE);
        firstSet.put("filterOp",filterOp);

        //frameOp
        HashSet<Kind> frameOp = new HashSet<>();
        frameOp.add(Kind.KW_SHOW);
        frameOp.add(Kind.KW_HIDE);
        frameOp.add(Kind.KW_MOVE);
        frameOp.add(Kind.KW_XLOC);
        frameOp.add(Kind.KW_YLOC);
        firstSet.put("frameOp",frameOp);

        //imageOp
        HashSet<Kind> imageOp = new HashSet<>();
        imageOp.add(Kind.OP_WIDTH);
        imageOp.add(Kind.OP_HEIGHT);
        imageOp.add(Kind.KW_SCALE);
        firstSet.put("imageOp",imageOp);


        //relOp
        HashSet<Kind> relOp = new HashSet<>();
        relOp.add(Kind.LT);
        relOp.add(Kind.LE);
        relOp.add(Kind.GT);
        relOp.add(Kind.GE);
        relOp.add(Kind.EQUAL);
        relOp.add(Kind.NOTEQUAL);
        firstSet.put("relOp",relOp);

        //strongOp
        HashSet<Kind> strongOp = new HashSet<>();
        strongOp.add(Kind.TIMES);
        strongOp.add(Kind.DIV);
        strongOp.add(Kind.AND);
        strongOp.add(Kind.MOD);
        firstSet.put("strongOp",strongOp);

        // weakOp
        HashSet<Kind> weakOp = new HashSet<>();
        weakOp.add(Kind.PLUS);
        weakOp.add(Kind.MINUS);
        weakOp.add(Kind.OR);
        firstSet.put("weakOp",weakOp);

        // chainElem
        HashSet<Kind> chainElem = new HashSet<>();
        chainElem.add(Kind.IDENT);
        chainElem.addAll(filterOp);
        chainElem.addAll(frameOp);
        chainElem.addAll(imageOp);
        firstSet.put("chainElem",chainElem);

        // statement
        HashSet<Kind> statement = new HashSet<>();
        statement.add(Kind.KW_WHILE);
        statement.add(Kind.OP_SLEEP);
        statement.add(Kind.KW_IF);
        // TODO add chain and assign, first of assign is IDENT which is included in chainElem
        statement.addAll(chainElem);
        firstSet.put("statement",statement);

    }

}
