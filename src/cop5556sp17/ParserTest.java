package cop5556sp17;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;


public class ParserTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		parser.factor();
	}

	@Test
	public void testArg() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,5) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
        parser.arg();
	}

	@Test
	public void testArgerror() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (3,) ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		thrown.expect(Parser.SyntaxException.class);
		parser.arg();
	}


	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog0 {}";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

	@Test
	public void testProgram1() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog1 { while(i < 10) { a <- 10; } }";
		Parser parser = new Parser(new Scanner(input).scan());
		parser.parse();
	}

    @Test
    public void testProgram2() throws IllegalCharException, IllegalNumberException, SyntaxException{
        String input = "prog1 { while(i < 10) { a <- 10 } }";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.parse();
    }

	@Test
	public void testProgram3() throws IllegalCharException, IllegalNumberException, SyntaxException{
		String input = "prog3 cd;";
		Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.parse();
    }

    @Test
    public void testProgram4() throws IllegalCharException, IllegalNumberException, SyntaxException{
        String input = "prog4 { if(a > b) { a <- b; }}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram5() throws IllegalCharException, IllegalNumberException, SyntaxException{
        String input = "prog5 { if(a > b) { a <- b; }";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.parse();
    }

    @Test
    public void testProgram6() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = " $prog {} ";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testProgram7() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = " _prog boolean $i , file $i2  { integer i while(i <= m + 9) { } } ";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = " true ";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.factor();
    }

    @Test
    public void testFactor2() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = " \n(screenheight*screenwidth*123+screenheight/screenwidth-1234 != area  <= screenArea) ";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.factor();
    }

    @Test
    public void testBlock1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.block();
    }

    @Test
    public void testBlock2() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{abc}";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.block();
    }

    @Test
    public void testBlock3() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{frame abc}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.block();
    }

    @Test
    public void testBlock4() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{sleep false}";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.block();
    }

    @Test
    public void testBlock5() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{\nsleep true;\n}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.block();
    }

    @Test
    public void testBlock6() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{ if (true) {}}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.block();
    }

    @Test
    public void testBlock7() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{ if (true) {integer xyz}}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.block();
    }

    @Test
    public void testBlock8() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{ xyz;}";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.block();
    }

    @Test
    public void testBlock9() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{move -> abc |-> show(true)}";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.block();
    }

    @Test
    public void testBlock10() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{move -> abc |-> show(true)};";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.block();
    }

    @Test
    public void testBlock11() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{move (true) -> show(true);}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.block();
    }

    @Test
    public void testBlock12() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "{move true -> \nxyz}";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.block();
    }

    @Test
    public void testArg1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "( x != 8)";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.arg();
    }

    @Test
    public void testArg2() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "( x != 10";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.arg();
    }

    @Test
    public void testArg3() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "( x >= 3 \n, y < 5, z \n == a)";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.arg();
    }

    @Test
    public void testArg4() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x != 0)";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.arg();
    }

    @Test
    public void testArg5() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "(u + v , x - y,)";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.arg();
    }

    @Test
    public void testArg6() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "()";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.arg();
    }

    @Test
    public void testArg7() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "( x \n,y)";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.arg();
    }


    @Test
    public void testWhile1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = " prog { while ( a < 111){ boolean x /* comment **\n** \n ccc */ sleep 5\n;}}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testIf1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = " prog { if (i < 10) { boolean x sleep 5\n;}}";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.parse();
    }

    @Test
    public void testDec1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "boolean x ";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.dec();
    }

    @Test
    public void testDec2() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "boolean ";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(SyntaxException.class);
        parser.dec();
    }

    @Test
    public void testDec3() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "boolean a x";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.dec();
    }

    @Test
    public void testExpression1() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "X";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression2() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "10000";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression3() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "!=";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.expression();
    }

    @Test
    public void testExpression4() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "/";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.expression();
    }

    @Test
    public void testExpression5() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "-";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.expression();
    }

    @Test
    public void testExpression6() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x 1000";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression7() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x + 20";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression8() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x / 20";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression9() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "abc == pqr";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression10() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "10 % 5 + 1001 % 7 + 33 / 2";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression11() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x | false & true";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression12() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "a - 5 /* comment1 */ >= b - 5 /* comment2 */ ";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testExpression13() throws IllegalCharException, IllegalNumberException, SyntaxException {
        String input = "x | (y >= 200)";
        Parser parser = new Parser(new Scanner(input).scan());
        parser.expression();
    }

    @Test
    public void testEmptyProgram() throws IllegalCharException, IllegalNumberException, SyntaxException{
        String input = "";
        Parser parser = new Parser(new Scanner(input).scan());
        thrown.expect(Parser.SyntaxException.class);
        parser.parse();
    }
}
