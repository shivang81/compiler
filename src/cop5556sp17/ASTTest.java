package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.Parser.SyntaxException;
import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.IllegalNumberException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.EQUAL;
import static cop5556sp17.Scanner.Kind.IDENT;
import static cop5556sp17.Scanner.Kind.LBRACE;
import static cop5556sp17.Scanner.Kind.NOTEQUAL;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.PLUS;
import static org.junit.Assert.assertEquals;


public class ASTTest {

	static final boolean doPrint = true;
	static void show(Object s){
		if(doPrint){System.out.println(s);}
	}


	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testFactor0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IdentExpression.class, ast.getClass());
	}

	@Test
	public void testFactor1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "123";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(IntLitExpression.class, ast.getClass());
	}


	@Test
	public void testBinaryExpr0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "1+abc";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(IntLitExpression.class, be.getE0().getClass());
		assertEquals(IdentExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}


	@Test
	public void testBinaryExpr2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "43 * 54 + 59";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(BinaryExpression.class, be.getE0().getClass());
		assertEquals(IntLitExpression.class, be.getE1().getClass());
		assertEquals(PLUS, be.getOp().kind);
	}


	@Test
	public void testBinaryExpr3() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "(x * y * z) <= (3+9*i) == true";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.expression();
		assertEquals(BinaryExpression.class, ast.getClass());
		BinaryExpression be = (BinaryExpression) ast;
		assertEquals(BinaryExpression.class, be.getE0().getClass());
		assertEquals(BooleanLitExpression.class, be.getE1().getClass());
		assertEquals(EQUAL, be.getOp().kind);

	}

	@Test
	public void testTuple0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "  (gator*florida, x*20,(x/y) < (i*j) == false, x >= y )";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.arg();
		assertEquals(Tuple.class, ast.getClass());
		Tuple tu = (Tuple) ast;
		assertEquals(4, tu.getExprList().size());
		assertEquals(BinaryExpression.class, tu.getExprList().get(0).getClass());
		assertEquals(BinaryExpression.class, tu.getExprList().get(1).getClass());
		assertEquals(BinaryExpression.class, tu.getExprList().get(2).getClass());
		assertEquals(BinaryExpression.class, tu.getExprList().get(2).getClass());
		assertEquals("(", tu.getFirstToken().getText());
	}

	@Test
	public void testTuple1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.arg();
		assertEquals(Tuple.class, ast.getClass());
		Tuple tu = (Tuple) ast;
		assertEquals(0, tu.getExprList().size());
		assertEquals("eof", tu.getFirstToken().kind.getText());
	}

	@Test
	public void testBlock1() throws IllegalCharException,IllegalNumberException, SyntaxException{
		String input = "{width (c, 20) -> x |-> convolve; p <- (q != 100) + 20; }";
		Parser parser = new Parser(new Scanner(input).scan());
		ASTNode ast = parser.block();
		Block block = (Block) ast;
		assertEquals(Block.class, ast.getClass());
		assertEquals(BinaryChain.class,block.getStatements().get(0).getClass());
		assertEquals(AssignmentStatement.class,block.getStatements().get(1).getClass());
		BinaryChain bChain = (BinaryChain) block.getStatements().get(0);
		AssignmentStatement astmt = (AssignmentStatement) block.getStatements().get(1);
		assertEquals(FilterOpChain.class,bChain.getE1().getClass());
		assertEquals(BARARROW,bChain.getArrow().kind);
		BinaryChain bChain1 = (BinaryChain) bChain.getE0();
		assertEquals(ImageOpChain.class,bChain1.getE0().getClass());
		assertEquals(ARROW,bChain1.getArrow().kind);
		assertEquals(IdentChain.class,bChain1.getE1().getClass());
		ImageOpChain img = (ImageOpChain) bChain1.getE0();
		assertEquals(OP_WIDTH,img.firstToken.kind);
		Tuple tp = img.getArg();
		assertEquals(IdentExpression.class,tp.getExprList().get(0).getClass());
		assertEquals(IntLitExpression.class,tp.getExprList().get(1).getClass());
		assertEquals(IDENT, astmt.var.firstToken.kind);
		assertEquals(BinaryExpression.class,astmt.getE().getClass());
		BinaryExpression bExp = (BinaryExpression) astmt.getE();
		assertEquals(PLUS,bExp.getOp().kind);
		assertEquals(IntLitExpression.class,bExp.getE1().getClass());
		assertEquals(BinaryExpression.class,bExp.getE0().getClass());
		BinaryExpression bExp1 = (BinaryExpression) bExp.getE0();
		assertEquals(NOTEQUAL,bExp1.getOp().kind);
		assertEquals(IdentExpression.class,bExp1.getE0().getClass());
		assertEquals(IntLitExpression.class,bExp1.getE1().getClass());
	}

	@Test
	public void testBlock2() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = " { if(k)" + "\n" + "{ x <- y; }" + "\n" + " }  ";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.block();
		Block blockVal = (Block) ast;
		assertEquals(Block.class, ast.getClass());
		assertEquals(LBRACE, blockVal.firstToken.kind);
		IfStatement ifStmt = (IfStatement) blockVal.getStatements().get(0);
		assertEquals(IdentExpression.class, ifStmt.getE().getClass());
		assertEquals(Block.class, ifStmt.getB().getClass());
		Block innerBlock = ifStmt.getB();
		assertEquals(AssignmentStatement.class, innerBlock.getStatements().get(0).getClass());
	}

	@Test
	public void testIfStatement1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "_$ { if (true) {/*comments*/ } }";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.program();
		Program programParser = (Program) ast;
		assertEquals(Program.class, ast.getClass());
		assertEquals(IDENT, programParser.getFirstToken().kind);
		assertEquals(IfStatement.class,programParser.getB().getStatements().get(0).getClass());
	}

	@Test
	public void testWhileStatement1() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "_$ { while (false) {/*comments*/ } }";
		Scanner scanner = new Scanner(input);
		scanner.scan();
		Parser parser = new Parser(scanner);
		ASTNode ast = parser.program();
		Program programParser = (Program) ast;
		assertEquals(Program.class, ast.getClass());
		assertEquals(IDENT, programParser.getFirstToken().kind);
		assertEquals(WhileStatement.class,programParser.getB().getStatements().get(0).getClass());
	}


	@Test
	public void testProgram0() throws IllegalCharException, IllegalNumberException, SyntaxException {
		String input = "program {}";
		Parser parser = new Parser(new Scanner(input).scan());
		ASTNode ast = parser.parse();
		Program programParser = (Program) ast;
		assertEquals(Program.class, ast.getClass());
		assertEquals(IDENT, programParser.getFirstToken().kind);
	}
}
