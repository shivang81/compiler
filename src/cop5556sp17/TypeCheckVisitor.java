package cop5556sp17;

import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;
import cop5556sp17.Scanner.Token;

import java.util.List;

import static cop5556sp17.AST.Type.TypeName.BOOLEAN;
import static cop5556sp17.AST.Type.TypeName.FILE;
import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.NONE;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SCALE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, arg);
		binaryChain.getE1().visit(this, arg);
		Token t1 = binaryChain.getE1().getFirstToken();
		String arrow = binaryChain.getArrow().getText();
		TypeName type = binaryChain.getE1().getTypeName();
		switch (binaryChain.getE0().getTypeName()) {
			case URL:
			case FILE:
				if(arrow.equals(ARROW.getText()) && type == IMAGE) {
					binaryChain.setTypeName(IMAGE);
				} else {
					throw new TypeCheckException("Invalid op / chainelem for URL / FILE in binary chain.");
				}
				break;
			case FRAME:
				if(arrow.equals(ARROW.getText()) &&
						t1.kind == KW_XLOC || t1.kind == KW_YLOC) {
					binaryChain.setTypeName(INTEGER);
				} else if(arrow.equals(ARROW.getText()) &&
						t1.kind == KW_SHOW || t1.kind == KW_HIDE || t1.kind == KW_MOVE) {
					binaryChain.setTypeName(FRAME);
				} else {
					throw new TypeCheckException("Invalid op / chainelem for FRAME in binary chain.");
				}
				break;
			case IMAGE:
				if(arrow.equals(ARROW.getText()) &&
						t1.kind == OP_WIDTH || t1.kind == OP_HEIGHT) {
					binaryChain.setTypeName(INTEGER);
				} else if(arrow.equals(ARROW.getText()) && type == FRAME) {
					binaryChain.setTypeName(FRAME);
				} else if(arrow.equals(ARROW.getText()) && type == FILE) {
					binaryChain.setTypeName(NONE);
				} else if(arrow.equals(ARROW.getText()) || arrow.equals(BARARROW.getText()) && binaryChain.getE1()
						instanceof FilterOpChain && t1.kind == OP_WIDTH || t1.kind == OP_GRAY ||
						t1.kind == OP_BLUR || t1.kind == OP_CONVOLVE) {
					binaryChain.setTypeName(IMAGE);
				} else if(arrow.equals(ARROW.getText()) &&
						t1.kind == KW_SCALE) {
					binaryChain.setTypeName(IMAGE);
				} else if(arrow.equals(ARROW.getText()) && binaryChain.getE1() instanceof IdentChain && type == IMAGE) {
					binaryChain.setTypeName(IMAGE);
				} else {
					throw new TypeCheckException("Invalid op / chainelem for FRAME in binary chain.");
				}
				break;
			case INTEGER:
				if(arrow.equals(ARROW.getText()) && binaryChain.getE1() instanceof IdentChain && type == INTEGER) {
					binaryChain.setTypeName(INTEGER);
				} else {
					throw new TypeCheckException("Invalid op / chainelem for INTEGER in binary chain.");
				}
				break;
			default:
				throw new TypeCheckException("Invalid chain type in binary chain.");
		}
		return arg;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		TypeName E0Type = binaryExpression.getE0().getTypeName();
		TypeName E1Type = binaryExpression.getE1().getTypeName();
		switch (binaryExpression.getOp().getText()) {
			case "+":
			case "-":
				if (E0Type == TypeName.INTEGER && E1Type == TypeName.INTEGER) {
					binaryExpression.setTypeName(INTEGER);
				} else if (E0Type == TypeName.IMAGE && E1Type == TypeName.IMAGE) {
					binaryExpression.setTypeName(IMAGE);
				} else {
					throw new TypeCheckException("Illegal operation for PLUS / MINUS");
				}
				break;
			case ">":
			case "<":
			case ">=":
			case "<=":
				if (E0Type == TypeName.INTEGER && E1Type == TypeName.INTEGER) {
					binaryExpression.setTypeName(BOOLEAN);
				} else if (E0Type == TypeName.BOOLEAN && E1Type == TypeName.BOOLEAN) {
					binaryExpression.setTypeName(BOOLEAN);
				} else {
					throw new TypeCheckException("Illegal operation for LE/GE/GT/LT.");
				}
				break;
			case "==":
			case "!=":
				if (E0Type == E1Type) {
					binaryExpression.setTypeName(BOOLEAN);
				} else {
					throw new TypeCheckException("EQUAL/NOT EQUAL not operated on same types.");
				}
				break;
			case "*":
			case "/":
			case "%":
				if (E0Type == TypeName.INTEGER && E1Type == TypeName.INTEGER)
					binaryExpression.setTypeName(INTEGER);
				else if (E0Type == TypeName.INTEGER && E1Type == TypeName.IMAGE)
					binaryExpression.setTypeName(IMAGE);
				else if (E0Type == TypeName.IMAGE && E1Type == TypeName.INTEGER)
					binaryExpression.setTypeName(IMAGE);
				else
					throw new TypeCheckException("Illegal operation for TIMES/DIV.");
				break;
			case "&":
			case "|":
				if (E0Type == TypeName.BOOLEAN && E1Type == TypeName.BOOLEAN) {
					binaryExpression.setTypeName(BOOLEAN);
				}
				break;
			default:
				throw new TypeCheckException("Invalid op found in binary expression.");
		}
		return arg;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		for(Dec dec : block.getDecs()) {
			dec.visit(this, arg);
		}
		for(Statement statement : block.getStatements())  {
			statement.visit(this, arg);
		}
		symtab.leaveScope();
		return arg;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.setTypeName(BOOLEAN);
		return arg;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		Tuple tuple = filterOpChain.getArg();
		tuple.visit(this, arg);
		if(tuple.getExprList().size() != 0)
			throw new TypeCheckException("Tuple length is not equal to 0.");
		filterOpChain.setTypeName(IMAGE);
		return arg;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Tuple tuple = frameOpChain.getArg();
		tuple.visit(this, arg);
		if (frameOpChain.getFirstToken().isKind(KW_SHOW) || frameOpChain.getFirstToken().isKind(KW_HIDE)) {
			if(tuple.getExprList().size() != 0)
				throw new TypeCheckException("Tuple length is not equal to 0.");
			frameOpChain.setTypeName(NONE);
		}
		else if (frameOpChain.getFirstToken().isKind(KW_XLOC) || frameOpChain.getFirstToken().isKind(KW_YLOC)) {
			if(tuple.getExprList().size() != 0)
				throw new TypeCheckException("Tuple length is not equal to 0.");
			frameOpChain.setTypeName(INTEGER);
		} else if (frameOpChain.getFirstToken().isKind(KW_MOVE)) {
			if (tuple.getExprList().size() != 2)
				throw new TypeCheckException("Tuple length is not equal to 0.");
			frameOpChain.setTypeName(NONE);
		}
		return arg;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec dec = symtab.lookup(identChain.getFirstToken().getText());
		if(dec == null)
			throw new TypeCheckException("Identifier not declared.");
		identChain.setDec(dec);
		identChain.setTypeName(dec.getTypeName());
		return arg;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = symtab.lookup(identExpression.getFirstToken().getText());
		if(dec == null)
			throw new TypeCheckException("Identifier not declared.");
		identExpression.setTypeName(dec.getTypeName());
		identExpression.setDec(dec);
		return arg;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Expression expression = ifStatement.getE();
		Block block = ifStatement.getB();
		expression.visit(this, arg);
		block.visit(this, arg);
		if(expression.getTypeName() != BOOLEAN)
			throw new TypeCheckException("Illegal expression type in if statement.");
		return arg;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.setTypeName(INTEGER);
		return arg;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression expression = sleepStatement.getE();
		expression.visit(this, arg);
		if(expression.getTypeName() != INTEGER) {
			throw new TypeCheckException("Illegal type of expression for sleep statement.");
		}
		return arg;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Expression expression = whileStatement.getE();
		Block block = whileStatement.getB();
		expression.visit(this, arg);
		block.visit(this, arg);
		if(expression.getTypeName() != BOOLEAN)
			throw new TypeCheckException("Illegal expression type in while statement.");
		return arg;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		TypeName t = Type.getTypeName(declaration.getFirstToken());
		declaration.setTypeName(t);
		boolean insertSuccess = symtab.insert(declaration.getIdent().getText(), declaration);
		if(!insertSuccess)
			throw new TypeCheckException("Insert failed due to duplicate entry.");
		return arg;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		List<ParamDec> paramDecs = program.getParams();
		Block block = program.getB();
		for(ParamDec paramDec : paramDecs) {
			paramDec.visit(this, arg);
		}
		block.visit(this, arg);
		return arg;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getVar().visit(this, arg);
		assignStatement.getE().visit(this, arg);
		IdentLValue identLValue = assignStatement.getVar();
		Expression e = assignStatement.getE();
		TypeName t1 = identLValue.getDec().getTypeName();
		TypeName t2 = e.getTypeName();
		if(t1 != t2)
			throw new TypeCheckException("The types of IdentLValue and Expression do not match");
		return arg;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec dec = symtab.lookup(identX.getFirstToken().getText());
		if(dec == null)
			throw new TypeCheckException("Identifier not declared.");
		identX.setDec(dec);
		return arg;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		TypeName t= Type.getTypeName(paramDec.getFirstToken());
		paramDec.setTypeName(t);
		boolean insertSuccess = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(!insertSuccess)
			throw new TypeCheckException("Insert failed due to duplicate entry.");
		return arg;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		constantExpression.setTypeName(INTEGER);
		return arg;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Tuple tuple = imageOpChain.getArg();
		tuple.visit(this, arg);
		if (imageOpChain.getFirstToken().isKind(OP_WIDTH) || imageOpChain.getFirstToken().isKind(OP_HEIGHT)) {
			if(tuple.getExprList().size() != 0)
				throw new TypeCheckException("Tuple length is not equal to 0.");
			imageOpChain.setTypeName(INTEGER);
		}
		else if (imageOpChain.getFirstToken().isKind(KW_SCALE)) {
			if(tuple.getExprList().size() != 1)
				throw new TypeCheckException("Tuple length is not equal to 0.");
			imageOpChain.setTypeName(IMAGE);
		}
		return arg;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> expressions = tuple.getExprList();
		for(Expression expression : expressions) {
			expression.visit(this, arg);
			if(expression.getTypeName() != INTEGER)
				throw new TypeCheckException("Invalid type of expression in tuple. Should be INTEGER.");
		}

		return arg;
	}

}
