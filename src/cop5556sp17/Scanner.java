package cop5556sp17;

import java.util.ArrayList;
import java.util.Collections;

public class Scanner {
    ArrayList<Integer> linesList = new ArrayList<>();
    boolean commentInFile = false;

    /**
     * Kind enum
     */

    public static enum Kind {
        IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"),
        KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"),
        KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"),
        SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"),
        RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"),
        EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="),
        PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"),
        ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"),
        KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"),
        OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"),
        KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"),
        KW_SCALE("scale"), EOF("eof");

        Kind(String text) {
            this.text = text;
        }

        final String text;

        String getText() {
            return text;
        }

        public static Kind getType(String text) {
            if (text != null) {
                for (Kind k : Kind.values()) {
                    if (text.equals(k.text)) {
                        return k;
                    }
                }
            }
            return null;
        }
    }

    public static enum State {
        START,
        IN_IDENTIFIER,
        IN_DIGIT,
        AFTER_EQUAL,
        AFTER_MINUS,
        AFTER_LT,
        AFTER_GT,
        AFTER_PIPE,
        AFTER_DIV,
        AFTER_NOT
    }
    /**
     * Thrown by Scanner when an illegal character is encountered
     */
    @SuppressWarnings("serial")
    public static class IllegalCharException extends Exception {
        public IllegalCharException(String message) {
            super(message);
        }
    }

    /**
     * Thrown by Scanner when an int literal is not a value that can be represented by an int.
     */
    @SuppressWarnings("serial")
    public static class IllegalNumberException extends Exception {
        public IllegalNumberException(String message){
            super(message);
        }
    }


    /**
     * Holds the line and position in the line of a token.
     */
    static class LinePos {
        public final int line;
        public final int posInLine;

        public LinePos(int line, int posInLine) {
            super();
            this.line = line;
            this.posInLine = posInLine;
        }

        @Override
        public String toString() {
            return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
        }
    }




    public class Token {
        public final Kind kind;
        public final int pos;  //position in input array
        public final int length;

        //returns the text of this Token
        public String getText() {
            return chars.substring(this.pos, this.pos + length);
        }

        //returns a LinePos object representing the line and column of this Token
        LinePos getLinePos(){
            int line = Collections.binarySearch(linesList, pos) + 1;
            if(line < 0)
                line = Math.abs(line);
            int position = pos;
            if(line > 1)
                position = pos - linesList.get(line - 1);
            return new LinePos(line, position);
        }

        Token(Kind kind, int pos, int length) {
            this.kind = kind;
            this.pos = pos;
            this.length = length;
        }

        /**
         * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
         * Note that the validity of the input should have been checked when the Token was created.
         * So the exception should never be thrown.
         *
         * @return  int value of this token, which should represent an INT_LIT
         * @throws NumberFormatException
         */
        public int intVal() throws NumberFormatException{
            return Integer.parseInt(chars.substring(this.pos, this.pos + length));

        }

        public boolean isKind(Scanner.Kind kind){
            if(kind == this.kind)
                return true;
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((kind == null) ? 0 : kind.hashCode());
            result = prime * result + length;
            result = prime * result + pos;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Token)) {
                return false;
            }
            Token other = (Token) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (kind != other.kind) {
                return false;
            }
            if (length != other.length) {
                return false;
            }
            if (pos != other.pos) {
                return false;
            }
            return true;
        }



        private Scanner getOuterType() {
            return Scanner.this;
        }

    }

    boolean isPrevMinus = false;

    Scanner(String chars) {
        this.chars = chars;
        tokens = new ArrayList<Token>();


    }

    private void addToken(Kind kind, int startPos, int length) {
        isPrevMinus = kind.equals(Kind.MINUS);
        tokens.add(new Token(kind, startPos, length));
    }
        

    /**
     * Initializes Scanner object by traversing chars and adding tokens to tokens list.
     *
     * @return this scanner
     * @throws IllegalCharException
     * @throws IllegalNumberException
     */
    public Scanner scan() throws IllegalCharException, IllegalNumberException {
        int pos = 0;
        //TODO IMPLEMENT THIS!!!!
        int length = chars.length();
        int startPos = 0;
        int ch;
        State state = State.START;

        while (pos <= length) {
            ch = pos < length ? chars.charAt(pos) : -1;
            switch (state) {
                case START:
                    pos = skipWhiteSpace(pos);
                    ch = pos < length ? chars.charAt(pos) : -1;
                    startPos = pos;
                    switch (ch) {
                        case -1: {addToken(Kind.EOF, pos, 0); pos++;}  break;
                        case '+': {addToken(Kind.PLUS, startPos, 1);pos++;} break;
                        case '*': {addToken(Kind.TIMES, startPos, 1);pos++;}break;
                        case '=': {state = State.AFTER_EQUAL;pos++;}break;
                        case '0': {addToken(Kind.INT_LIT,startPos, 1);pos++;}break;
                        case ';': {addToken(Kind.SEMI,startPos, 1);pos++;}break;
                        case '(': {addToken(Kind.LPAREN, startPos, 1);pos++;}break;
                        case ')': {addToken(Kind.RPAREN, startPos, 1);pos++;}break;
                        case ',': {addToken(Kind.COMMA, startPos, 1);pos++;}break;
                        case '{': {addToken(Kind.LBRACE, startPos, 1);pos++;}break;
                        case '}': {addToken(Kind.RBRACE, startPos, 1);pos++;}break;
                        case '-': {state = State.AFTER_MINUS;pos++;}break;
                        case '|': {state = State.AFTER_PIPE;pos++;}break;
                        case '&': {addToken(Kind.AND, startPos, 1);pos++;}break;
                        case '!': {state = State.AFTER_NOT;pos++;}break;
                        case '<': {state = State.AFTER_LT;pos++;}break;
                        case '>': {state = State.AFTER_GT;pos++;}break;
                        case '/': {state = State.AFTER_DIV;pos++;}break;
                        case '%': {addToken(Kind.MOD, startPos, 1);pos++;}break;
                        case '\n': {state = State.START;pos++; linesList.add(pos);}break;

                        default: {
                            if (Character.isDigit(ch)) {state = State.IN_DIGIT;pos++;}
                            else if (Character.isJavaIdentifierStart(ch)) {
                                state = State.IN_IDENTIFIER;pos++;
                            }
                            else {throw new IllegalCharException(
                                    "illegal char " +(char) ch+" at pos "+pos);
                            }
                        }
                    } // switch (ch)
                    break;
                case IN_DIGIT:
                    if (Character.isDigit(ch)) {
                        pos++;
                    } else {
                        try {
                            if (!isPrevMinus)
                                Integer.parseInt(chars.substring(startPos, pos));
                            else
                                Integer.parseInt("-" + chars.substring(startPos, pos));
                        } catch (NumberFormatException e) {
                            throw  new IllegalNumberException("Illegal number " + chars.substring(startPos, pos));
                        }
                        addToken(Kind.INT_LIT, startPos, pos - startPos);
                        state = State.START;
                    }
                    break;
                case IN_IDENTIFIER:
                    if (Character.isJavaIdentifierPart(ch)) {
                        pos++;
                    } else {
                        Kind k = Kind.getType(chars.substring(startPos, pos));
                        if(k != null) {
                            addToken(k, startPos, pos - startPos);
                        } else {
                            addToken(Kind.IDENT, startPos, pos - startPos);
                        }
                        state = State.START;
                    }
                    break;
                case AFTER_EQUAL:
                    if(ch == '=') {
                        addToken(Kind.EQUAL, startPos, 2);
                        pos++;
                        state = State.START;
                    } else {
                        int line = linesList.size() + 1;
                        int position = pos;
                        if(linesList.size() > 0)
                            position = pos - linesList.get(linesList.size() - 1);
                        LinePos linePos = new LinePos(line, position);
                        throw new IllegalCharException("Invalid token. Expected = after an = at linePos=" + linePos.toString());
                    }
                    break;
                case AFTER_DIV:
                    if(ch == '*') {
                        commentInFile = true;
                        while(pos + 2 < chars.length() && !(chars.charAt(pos + 1) == '*' && chars.charAt(pos + 2) == '/')) {
                            if(chars.charAt(pos) == '\n') {
                                pos++;
                                linesList.add(pos);
                            } else {
                                pos++;
                            }
                        }
                        pos+=3;
                        state = State.START;
                    }
                    else {
                        addToken(Kind.DIV, startPos, 1);
                        state = State.START;
                    }
                    break;
                case AFTER_GT:
                    if(ch == '=') {
                        addToken(Kind.GE, startPos, 2);
                        pos++;
                    } else {
                        addToken(Kind.GT, startPos, 1);
                    }
                    state = State.START;
                    break;
                case AFTER_LT:
                    if(ch == '=') {
                        addToken(Kind.LE, startPos, 2);
                        pos++;
                    } else if(ch == '-') {
                        addToken(Kind.ASSIGN, startPos, 2);
                        pos++;
                    } else {
                        addToken(Kind.LT, startPos, 1);
                    }
                    state = State.START;
                    break;
                case AFTER_MINUS:
                    if(ch == '>') {
                        addToken(Kind.ARROW, startPos, 2);
                        pos++;
                    } else {
                        addToken(Kind.MINUS, startPos, 1);
                    }
                    state = State.START;
                    break;
                case AFTER_NOT:
                    if(ch == '=') {
                        addToken(Kind.NOTEQUAL, startPos, 2);
                        pos++;
                    } else {
                        addToken(Kind.NOT, startPos, 1);
                    }
                    state = State.START;
                    break;
                case AFTER_PIPE:
                    if(ch == '-') {
                        pos++;
                        ch = pos < length ? chars.charAt(pos) : -1;
                        if(ch == '>') {
                            addToken(Kind.BARARROW, startPos, 3);
                            pos++;
                        } else {
                            addToken(Kind.OR, startPos, 1);
                            pos--;
                        }
                    } else {
                        addToken(Kind.OR, startPos, 1);
                    }
                    state = State.START;
                    break;
                default:
            }
        }
        addToken(Kind.EOF, pos, 0);
        return this;
    }



    final ArrayList<Token> tokens;
    final String chars;
    int tokenNum;

    /*
     * Return the next token in the token list and update the state so that
     * the next call will return the Token..
     */
    public Token nextToken() {
        if (tokenNum >= tokens.size())
            return null;
        return tokens.get(tokenNum++);
    }

    /*
     * Return the next token in the token list without updating the state.
     * (So the following call to next will return the same token.)
     */
    public Token peek() {
        if (tokenNum >= tokens.size())
            return null;
        return tokens.get(tokenNum);
    }

    /**
     * Returns a LinePos object containing the line and position in line of the
     * given token.
     *
     * Line numbers start counting at 0
     *
     * @param t
     * @return
     */
    public LinePos getLinePos(Token t) {
        int line = Collections.binarySearch(linesList, t.pos) + 1;
        if(line < 0)
            line = Math.abs(line);
        int position = t.pos;
        if(line >= 1)
            position = t.pos - linesList.get(line - 1);
        return new LinePos(line, position);

    }

    private int skipWhiteSpace(int pos) {
        while (pos < chars.length() && Character.isWhitespace(chars.charAt(pos)) && chars.charAt(pos) != '\n')
            pos++;
        return pos;
    }


}
