package cop5556sp17;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static cop5556sp17.Scanner.Kind.AND;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.ASSIGN;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.COMMA;
import static cop5556sp17.Scanner.Kind.DIV;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.GE;
import static cop5556sp17.Scanner.Kind.GT;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.INT_LIT;
import static cop5556sp17.Scanner.Kind.KW_BOOLEAN;
import static cop5556sp17.Scanner.Kind.KW_FILE;
import static cop5556sp17.Scanner.Kind.KW_FRAME;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_IF;
import static cop5556sp17.Scanner.Kind.KW_IMAGE;
import static cop5556sp17.Scanner.Kind.KW_INTEGER;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_WHILE;
import static cop5556sp17.Scanner.Kind.LBRACE;
import static cop5556sp17.Scanner.Kind.LE;
import static cop5556sp17.Scanner.Kind.LPAREN;
import static cop5556sp17.Scanner.Kind.LT;
import static cop5556sp17.Scanner.Kind.MINUS;
import static cop5556sp17.Scanner.Kind.MOD;
import static cop5556sp17.Scanner.Kind.NOT;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_SLEEP;
import static cop5556sp17.Scanner.Kind.OR;
import static cop5556sp17.Scanner.Kind.PLUS;
import static cop5556sp17.Scanner.Kind.RBRACE;
import static cop5556sp17.Scanner.Kind.RPAREN;
import static cop5556sp17.Scanner.Kind.SEMI;
import static cop5556sp17.Scanner.Kind.TIMES;
import static org.junit.Assert.assertEquals;

public class ScannerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();



    @Test
    public void testEmpty() throws IllegalCharException, IllegalNumberException {
        String input = "";
        Scanner scanner = new Scanner(input);
        scanner.scan();
    }

    @Test
    public void testSemiConcat() throws IllegalCharException, IllegalNumberException {
        //input string
        String input = ";;;";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        //get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(SEMI, token.kind);
        assertEquals(0, token.pos);
        String text = SEMI.getText();
        assertEquals(text.length(), token.length);
        assertEquals(text, token.getText());
        //get the next token and check its kind, position, and contents
        Scanner.Token token1 = scanner.nextToken();
        assertEquals(SEMI, token1.kind);
        assertEquals(1, token1.pos);
        assertEquals(text.length(), token1.length);
        assertEquals(text, token1.getText());
        Scanner.Token token2 = scanner.nextToken();
        assertEquals(SEMI, token2.kind);
        assertEquals(2, token2.pos);
        assertEquals(text.length(), token2.length);
        assertEquals(text, token2.getText());
        //check that the scanner has inserted an EOF token at the end
        Scanner.Token token3 = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF,token3.kind);
    }

    @Test
    public void testIdentifier() throws IllegalCharException, IllegalNumberException {
        String input = "abc;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, SEMI);

    }

    @Test
    public void testKeywords() throws IllegalCharException, IllegalNumberException {
        String input = "boolean gray";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, KW_BOOLEAN);
        verifyNextToken(scanner, OP_GRAY);
    }

    @Test
    public void testEquals() throws IllegalCharException, IllegalNumberException {
        String input = "i1 == i2;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, EQUAL);
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, SEMI);
    }

    @Test(expected = IllegalCharException.class)
    public void testEqualsInvalid() throws IllegalCharException, IllegalNumberException {
        String input = "5=0";
        Scanner scanner = new Scanner(input);
        scanner.scan();
    }

    @Test
    public void testLessThan() throws IllegalCharException, IllegalNumberException {
        String input = "i1 <=< i2 <- ;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, LE);
        verifyNextToken(scanner, LT);
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, ASSIGN);
        verifyNextToken(scanner, SEMI);
    }

    @Test
    public void testMinus() throws IllegalCharException, IllegalNumberException {
        String input = "i1 - i2 -> i3;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, MINUS);
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, ARROW);
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, SEMI);
    }

    @Test
    public void testNot() throws IllegalCharException, IllegalNumberException {
        String input = "i1 ! i2 != i3;";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, NOT);
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, NOTEQUAL);
        verifyNextToken(scanner, IDENT);
        verifyNextToken(scanner, SEMI);
    }

    @Test
    public void testPipe() throws IllegalCharException, IllegalNumberException {
        //input string
        String input = "i1 | i2 |-> i3;";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        //get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(IDENT, token.kind);

        Scanner.Token token2 = scanner.nextToken();
        assertEquals(OR, token2.kind);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(IDENT, token3.kind);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(BARARROW, token4.kind);

        Scanner.Token token5 = scanner.nextToken();
        assertEquals(IDENT, token5.kind);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(SEMI, token6.kind);
    }

    @Test
    public void testPipe2() throws IllegalCharException, IllegalNumberException {
        //input string
        String input = "i1 | i2 |- i3;";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        //get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(IDENT, token.kind);

        Scanner.Token token2 = scanner.nextToken();
        assertEquals(OR, token2.kind);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(IDENT, token3.kind);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(OR, token4.kind);

        Scanner.Token token5 = scanner.nextToken();
        assertEquals(MINUS, token5.kind);

        Scanner.Token token7 = scanner.nextToken();
        assertEquals(IDENT, token7.kind);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(SEMI, token6.kind);
    }

    @Test
    public void testWhile() throws IllegalCharException, IllegalNumberException {
        //input string
        String input = "while(i < 23) {sleep}";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        //get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(KW_WHILE, token.kind);

        Scanner.Token token2 = scanner.nextToken();
        assertEquals(LPAREN, token2.kind);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(IDENT, token3.kind);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(LT, token4.kind);

        Scanner.Token token5 = scanner.nextToken();
        assertEquals(INT_LIT, token5.kind);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(RPAREN, token6.kind);

        Scanner.Token token7 = scanner.nextToken();
        assertEquals(LBRACE, token7.kind);

        Scanner.Token token8 = scanner.nextToken();
        assertEquals(OP_SLEEP, token8.kind);

        Scanner.Token token9 = scanner.nextToken();
        assertEquals(RBRACE, token9.kind);
    }

    @Test
    public void test1() throws IllegalCharException, IllegalNumberException {
        //input string
        String input = "while (i < 23)" + "\n" +
                " {" + "\n" +
                "sleep" + " abc\n" +
                "}";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        //get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(KW_WHILE, token.kind);

        Scanner.Token token2 = scanner.nextToken();
        assertEquals(LPAREN, token2.kind);

        Scanner.Token token3 = scanner.nextToken();
        assertEquals(IDENT, token3.kind);

        Scanner.Token token4 = scanner.nextToken();
        assertEquals(LT, token4.kind);

        Scanner.Token token5 = scanner.nextToken();
        assertEquals(INT_LIT, token5.kind);

        Scanner.Token token6 = scanner.nextToken();
        assertEquals(RPAREN, token6.kind);

        Scanner.Token token7 = scanner.nextToken();
        assertEquals(LBRACE, token7.kind);

        Scanner.Token token8 = scanner.nextToken();
        assertEquals(OP_SLEEP, token8.kind);

        Scanner.Token token10 = scanner.nextToken();
        assertEquals(IDENT, token10.kind);

        Scanner.Token token9 = scanner.nextToken();
        assertEquals(RBRACE, token9.kind);
    }

    @Test
    public void testComment() throws IllegalCharException, IllegalNumberException {
        //input string
        String input =  "/* */ dsd * /";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        //get the first token and check its kind, position, and contents
        Scanner.Token token = scanner.nextToken();
        assertEquals(IDENT, token.kind);

        Scanner.Token token1 = scanner.nextToken();
        assertEquals(TIMES, token1.kind);

        Scanner.Token token2 = scanner.nextToken();
        assertEquals(DIV, token2.kind);

    }

    @Test
    public void testComment2() throws IllegalCharException, IllegalNumberException {
        //input string
        String input =  "\n \n   /* sdsdsdsd */ dsd */";
        //create and initialize the scanner
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 21, "dsd");
        verifyNextToken(scanner, TIMES, 25);
        verifyNextToken(scanner, DIV, 26);
    }

    @Test
    public void testComment3() throws IllegalCharException, IllegalNumberException {
        String input = "/*5=0*/a b";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 7, "a");
        verifyNextToken(scanner, IDENT, 9, "b");
    }


    /**
     * This test illustrates how to check that the Scanner detects errors properly.
     * In this test, the input contains an int literal with a value that exceeds the range of an int.
     * The scanner should detect this and throw and IllegalNumberException.
     *
     * @throws IllegalCharException
     * @throws IllegalNumberException
     */
    @Test
    public void testIntOverflowError() throws IllegalCharException, IllegalNumberException{
        String input = "99999999999999999";
        Scanner scanner = new Scanner(input);
        thrown.expect(IllegalNumberException.class);
        scanner.scan();
    }

    @Test
    public void test2() throws IllegalCharException, IllegalNumberException {
        String input = "}{+)!(";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, RBRACE, 0);
        verifyNextToken(scanner, LBRACE, 1);
        verifyNextToken(scanner, PLUS, 2);
        verifyNextToken(scanner, RPAREN, 3);
        verifyNextToken(scanner, NOT, 4);
        verifyNextToken(scanner, LPAREN, 5);
        verifyEnd(scanner);
    }

    @Test
    public void test3() throws IllegalCharException, IllegalNumberException {
        String input = "!!!=!=!,";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, NOT, 0);
        verifyNextToken(scanner, NOT, 1);
        verifyNextToken(scanner, NOTEQUAL, 2);
        verifyNextToken(scanner, NOTEQUAL, 4);
        verifyNextToken(scanner, NOT, 6);
        verifyNextToken(scanner, COMMA, 7);
        verifyEnd(scanner);
    }

    @Test
    public void test4() throws IllegalCharException, IllegalNumberException {
        String input = "--->->-";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, MINUS, 0);
        verifyNextToken(scanner, MINUS, 1);
        verifyNextToken(scanner, ARROW, 2);
        verifyNextToken(scanner, ARROW, 4);
        verifyNextToken(scanner, MINUS, 6);
        verifyEnd(scanner);
    }

    @Test
    public void test5() throws IllegalCharException, IllegalNumberException {
        String input = "|;|--->->-|->";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, OR, 0);
        verifyNextToken(scanner, SEMI, 1);
        verifyNextToken(scanner, OR, 2);
        verifyNextToken(scanner, MINUS, 3);
        verifyNextToken(scanner, MINUS, 4);
        verifyNextToken(scanner, ARROW, 5);
        verifyNextToken(scanner, ARROW, 7);
        verifyNextToken(scanner, MINUS, 9);
        verifyNextToken(scanner, BARARROW, 10);
        verifyEnd(scanner);
    }

    @Test
    public void test6() throws IllegalCharException, IllegalNumberException {
        String input = "<<<=>>>=>< ->-->";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, LT, 0);
        verifyNextToken(scanner, LT, 1);
        verifyNextToken(scanner, LE, 2);
        verifyNextToken(scanner, GT, 4);
        verifyNextToken(scanner, GT, 5);
        verifyNextToken(scanner, GE, 6);
        verifyNextToken(scanner, GT, 8);
        verifyNextToken(scanner, LT, 9);
        verifyNextToken(scanner, ARROW, 11);
        verifyNextToken(scanner, MINUS, 13);
        verifyNextToken(scanner, ARROW, 14);
        verifyEnd(scanner);
    }

    @Test
    public void test7() throws IllegalCharException, IllegalNumberException {
        String input = "123()+4+54321";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, INT_LIT, 0, "123");
        verifyNextToken(scanner, LPAREN, 3);
        verifyNextToken(scanner, RPAREN, 4);
        verifyNextToken(scanner, PLUS, 5);
        verifyNextToken(scanner, INT_LIT, 6, "4");
        verifyNextToken(scanner, PLUS, 7);
        verifyNextToken(scanner, INT_LIT, 8, "54321");
        verifyEnd(scanner);
    }

    @Test
    public void test8() throws IllegalCharException, IllegalNumberException {
        String input = "a+b;a23a4";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 0, "a");
        verifyNextToken(scanner, PLUS, 1);
        verifyNextToken(scanner, IDENT, 2, "b");
        verifyNextToken(scanner, SEMI, 3);
        verifyNextToken(scanner, IDENT, 4, "a23a4");
        verifyEnd(scanner);
    }

    @Test
    public void test9() throws IllegalCharException, IllegalNumberException {
        String input = "ifwhile;if;while;boolean;boolean0;integer;integer32|->frame->-image";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 0, "ifwhile");
        verifyNextToken(scanner, SEMI, 7);
        verifyNextToken(scanner, KW_IF, 8);
        verifyNextToken(scanner, SEMI, 10);
        verifyNextToken(scanner, KW_WHILE, 11);
        verifyNextToken(scanner, SEMI, 16);
        verifyNextToken(scanner, KW_BOOLEAN, 17);
        verifyNextToken(scanner, SEMI, 24);
        verifyNextToken(scanner, IDENT, 25, "boolean0");
        verifyNextToken(scanner, SEMI, 33);
        verifyNextToken(scanner, KW_INTEGER, 34);
        verifyNextToken(scanner, SEMI, 41);
        verifyNextToken(scanner, IDENT, 42, "integer32");
        verifyNextToken(scanner, BARARROW, 51, "|->");
        verifyNextToken(scanner, KW_FRAME, 54);
        verifyNextToken(scanner, ARROW, 59);
        verifyNextToken(scanner, MINUS, 61);
        verifyNextToken(scanner, KW_IMAGE, 62);
        verifyEnd(scanner);
    }

    @Test
    public void test10() throws IllegalCharException, IllegalNumberException {
        String input = "abc 234 a23";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 0, "abc");
        verifyNextToken(scanner, INT_LIT, 4, "234");
        verifyNextToken(scanner, IDENT, 8, "a23");
        verifyEnd(scanner);
    }

    @Test
    public void test11() throws IllegalCharException, IllegalNumberException {
        String input = "abc! !d";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 0, "abc");
        verifyNextToken(scanner, NOT, 3);
        verifyNextToken(scanner, NOT, 5);
        verifyNextToken(scanner, IDENT, 6, "d");
        verifyEnd(scanner);
    }

    @Test
    public void test12() throws IllegalCharException, IllegalNumberException {
        String input = "   ;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, SEMI, 3);
        verifyEnd(scanner);
    }

    @Test
    public void test13() throws IllegalCharException, IllegalNumberException {
        String input = "\n\n \r;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, SEMI, 4);
        verifyEnd(scanner);
    }

    @Test
    public void test14() throws IllegalCharException, IllegalNumberException {
        String input = "a\nbc! !\nd";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token t = scanner.nextToken();
        assertEquals("a", t.getText());
        verifyPos(scanner, t, 0, 0);
        t = scanner.nextToken();
        assertEquals("bc", t.getText());
        verifyPos(scanner, t, 1, 0);
        t = scanner.nextToken();
        assertEquals("!", t.getText());
        verifyPos(scanner, t, 1, 2);
        t = scanner.nextToken();
        assertEquals("!", t.getText());
        verifyPos(scanner, t, 1, 4);
        t = scanner.nextToken();
        assertEquals("d", t.getText());
        verifyPos(scanner, t, 2, 0);



    }

    @Test
    public void test15() throws IllegalCharException, IllegalNumberException {
        String input = "/*...*/a/***/\nbc!/ /*/ /**/ !\nd/*.**/";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 7, "a");
        verifyNextToken(scanner, IDENT, 14, "bc");
        verifyNextToken(scanner, NOT, 16);
        verifyNextToken(scanner, DIV, 17);
        verifyNextToken(scanner, NOT, 28);
        verifyNextToken(scanner, IDENT, 30, "d");
        verifyEnd(scanner);
    }


    @Test
    public void test16error() throws IllegalCharException, IllegalNumberException{
        String input = "abc def/n345 #abc";
        Scanner scanner = new Scanner(input);
        thrown.expect(IllegalCharException.class);
        thrown.expectMessage("#");
        scanner.scan();
    }

    @Test
    public void test17error() throws IllegalCharException, IllegalNumberException{
        String input = "99999999999999999";
        Scanner scanner = new Scanner(input);
        thrown.expect(IllegalNumberException.class);
        scanner.scan();
    }

    @Test
    public void test18() throws IllegalCharException, IllegalNumberException{
        String input = "/* * ** */\nabc";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, IDENT, 11, "abc");
        verifyEnd(scanner);
    }

    @Test
    public void test19() throws IllegalCharException, IllegalNumberException{
        String input="123 456";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token t = scanner.nextToken();
        assertEquals(123, t.intVal());
        t = scanner.nextToken();
        assertEquals(456, t.intVal());
        verifyEnd(scanner);
    }

    @Test
    public void test20() throws IllegalCharException, IllegalNumberException {
        String input="***%&";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, TIMES, 0);
        verifyNextToken(scanner, TIMES, 1);
        verifyNextToken(scanner, TIMES, 2);
        verifyNextToken(scanner, MOD, 3);
        verifyNextToken(scanner, AND, 4);
    }

    @Test
    public void test21() throws IllegalCharException, IllegalNumberException{
        String input="";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyEnd(scanner);
    }

    @Test
    public void test22() throws IllegalCharException, IllegalNumberException{
        String input="/****";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyEnd(scanner);
    }

    @Test
    public void test23() throws IllegalCharException, IllegalNumberException{
        String input="== ==";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, EQUAL, 0);
        verifyNextToken(scanner, EQUAL, 3);
    }

    @Test
    public void test24() throws IllegalCharException, IllegalNumberException{
        thrown.expect(IllegalCharException.class);
        String input="=";
        Scanner scanner = new Scanner(input);
        scanner.scan();

    }

    @Test
    public void test25() throws IllegalCharException, IllegalNumberException{
        String input = "show\r\n hide \n move \n file";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token t0 = verifyNextToken(scanner, KW_SHOW, 0);
        verifyPos(scanner, t0, 0, 0);
        Scanner.Token t1 = verifyNextToken(scanner, KW_HIDE, 7);
        verifyPos(scanner, t1, 1, 1);
        Scanner.Token t2 = verifyNextToken(scanner, KW_MOVE, 14);
        verifyPos(scanner, t2, 2, 1);
        Scanner.Token t3 = verifyNextToken(scanner, KW_FILE, 21);


    }

    @Test
    public void test26() throws IllegalCharException, IllegalNumberException{
        String input = "show\r\n hide /*\n \n dfdf */ \n move \n file";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        Scanner.Token t0 = verifyNextToken(scanner, KW_SHOW, 0);
        verifyPos(scanner, t0, 0, 0);
        Scanner.Token t1 = verifyNextToken(scanner, KW_HIDE, 7);
        verifyPos(scanner, t1, 1, 1);
        Scanner.Token t2 = verifyNextToken(scanner, KW_MOVE, 28);
        verifyPos(scanner, t2, 4, 1);
        Scanner.Token t3 = verifyNextToken(scanner, KW_FILE, 35);
    }

    @Test
    public void test27() throws IllegalCharException, IllegalNumberException {
        String input = ";()(;";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, SEMI, 0);
        verifyNextToken(scanner, LPAREN, 1);
        verifyNextToken(scanner, RPAREN, 2);
        verifyNextToken(scanner, LPAREN, 3);
        verifyNextToken(scanner, SEMI, 4);
        verifyEnd(scanner);
    }

    @Test
    public void test28() throws IllegalCharException, IllegalNumberException {
        String input = "-2147483648";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, MINUS, 0);
        verifyNextToken(scanner, INT_LIT, 1, "2147483648");
        verifyEnd(scanner);
    }

    @Test
    public void test29() throws IllegalCharException, IllegalNumberException {
        String input = " 0_123/* \n new comment open but not closed ***";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, INT_LIT, 1, "0");
        verifyNextToken(scanner, IDENT, 2, "_123");
        verifyEnd(scanner);
    }


    @Test
    public void testAssign() throws IllegalCharException, IllegalNumberException{
        String input = "  -< <- <+ <= <\n-<--";
        Scanner scanner = new Scanner(input);
        scanner.scan();
        verifyNextToken(scanner, MINUS);
        verifyNextToken(scanner, LT);
        verifyNextToken(scanner, ASSIGN);
        verifyNextToken(scanner, LT);
        verifyNextToken(scanner, PLUS);
        verifyNextToken(scanner, LE);
        verifyNextToken(scanner, LT);
        verifyNextToken(scanner, MINUS);
        verifyNextToken(scanner, ASSIGN);
        verifyNextToken(scanner, MINUS);
    }

    @Test
    public void test223() throws IllegalCharException, IllegalNumberException{
        String input = "$0 /* qdgh \n qdgeiu***\n/*/ Boolean _0 ";
        Scanner scanner = new Scanner(input);
        scanner.scan();

        Scanner.Token t = scanner.nextToken();
        assertEquals("$0", t.getText());
        verifyPos(scanner, t, 0, 0);

        t = scanner.nextToken();
        assertEquals("Boolean", t.getText());
        assertEquals(Scanner.Kind.IDENT, t.kind);
        verifyPos(scanner, t, 2, 4);

        t = scanner.nextToken();
        assertEquals("_0", t.getText());
        verifyPos(scanner, t, 2, 12);
    }

    Scanner.Token verifyNextToken(Scanner scanner, Scanner.Kind kind, int pos, String text) {
        Scanner.Token token = scanner.nextToken();
        assertEquals(kind, token.kind);
        assertEquals(pos, token.pos);
        assertEquals(text.length(), token.length);
        assertEquals(text, token.getText());
        return token;
    }

    // Don't use this with idents or numlits
    Scanner.Token verifyNextToken(Scanner scanner, Scanner.Kind kind, int pos) {
        Scanner.Token token = scanner.nextToken();
        assertEquals(kind, token.kind);
        assertEquals(pos, token.pos);
        String text = kind.getText();
        assertEquals(text.length(), token.length);
        assertEquals(text, token.getText());
        return token;
    }

    Scanner.Token verifyNextToken(Scanner scanner, Scanner.Kind kind) {
        Scanner.Token token = scanner.nextToken();
        assertEquals(kind, token.kind);
        return token;
    }

    Scanner.Token verifyEnd(Scanner scanner) {
        Scanner.Token token = scanner.nextToken();
        assertEquals(Scanner.Kind.EOF, token.kind);
        return token;
    }

    void verifyPos(Scanner scanner, Scanner.Token t, int line, int posInLine){
        Scanner.LinePos p = scanner.getLinePos(t);
        assertEquals(line, p.line);
        assertEquals(posInLine, p.posInLine);
    }


}
