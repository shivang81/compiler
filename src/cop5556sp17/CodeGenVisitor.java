package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	private int slot = 0;
	private int i = 0;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);

		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		//TODO  visit the local variables
		for (Dec decl: program.getB().getDecs()) {
			int slotNumber = decl.getSlot();
			mv.visitLocalVariable(decl.getIdent().getText(), decl.getTypeName().getJVMTypeDesc(),
					null, startRun, endRun, slotNumber);
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		binaryExpression.getE0().visit(this,arg);
		binaryExpression.getE1().visit(this,arg);
		Label set = new Label();
		Label end = new Label();
		switch(binaryExpression.getOp().kind){
			case PLUS:
				mv.visitInsn(IADD);
				break;
			case MINUS:
				mv.visitInsn(ISUB);
				break;
			case OR:
				mv.visitInsn(IOR);
				break;
			case TIMES:
				mv.visitInsn(IMUL);
				break;
			case DIV:
				mv.visitInsn(IDIV);
				break;
			case AND:
				mv.visitInsn(IAND);
				break;
			case MOD:
				mv.visitInsn(IREM);
				break;
			case LT: {
				mv.visitJumpInsn(IF_ICMPLT, set);
				mv.visitLdcInsn(false);
			}
			break;
			case LE: {
				mv.visitJumpInsn(IF_ICMPLE, set);
				mv.visitLdcInsn(false);
			}
			break;
			case GT: {
				mv.visitJumpInsn(IF_ICMPGT, set);
				mv.visitLdcInsn(false);
			}
			break;
			case GE: {
				mv.visitJumpInsn(IF_ICMPGE, set);
				mv.visitLdcInsn(false);
			}
			break;
			case EQUAL: {
				mv.visitJumpInsn(IF_ICMPEQ, set);
				mv.visitLdcInsn(false);
			}
			break;
			case NOTEQUAL: {
				mv.visitJumpInsn(IF_ICMPNE, set);
				mv.visitLdcInsn(false);
			}
			break;
			default:
				break;
		}
		mv.visitJumpInsn(GOTO, end);
		mv.visitLabel(set);
		mv.visitLdcInsn(true);
		mv.visitLabel(end);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		Label blockStart = new Label();
		mv.visitLabel(blockStart);
		for (Dec dec : block.getDecs()) {
			dec.visit(this, null);
		}
		for (Statement statement : block.getStatements()) {
				if(statement instanceof AssignmentStatement) {
				if(((AssignmentStatement)statement).getVar().getDec() instanceof ParamDec) {
					mv.visitVarInsn(ALOAD, 0);
				}
			}
			statement.visit(this, null);
		}
		Label blockEnd = new Label();
		mv.visitLabel(blockEnd);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(booleanLitExpression.getValue());
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setSlot(slot++);
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec dec = identExpression.getDec();
		int slot = dec.getSlot();
		if(dec instanceof ParamDec) {
			mv.visitVarInsn(ALOAD, 0);
			if(dec.getTypeName() == TypeName.INTEGER)
				mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(), "I");
			else if(dec.getTypeName() == TypeName.BOOLEAN)
				mv.visitFieldInsn(GETFIELD, className, identExpression.getFirstToken().getText(), "Z");
		}
		else {
			mv.visitVarInsn(ILOAD, slot);
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec dec = identX.getDec();
		String fieldType = dec.getTypeName().getJVMTypeDesc();
		int slot = dec.getSlot();
		if(dec instanceof ParamDec) {
			CodeGenUtils.genPrint(DEVEL, mv, "\nlvalue.......: " + identX.getFirstToken().getText());
			mv.visitFieldInsn(PUTFIELD, className, identX.getFirstToken().getText(), fieldType);
		}
		else {
			mv.visitVarInsn(ISTORE, slot);
		}
		return null;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		Expression expression = ifStatement.getE();
		expression.visit(this, arg);
		Label ifEndLabel = new Label();
		mv.visitJumpInsn(IFEQ, ifEndLabel);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(ifEndLabel);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(intLitExpression.getFirstToken().intVal());
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//For assignment 5, only needs to handle integers and booleans
		FieldVisitor fieldVisitor ;
		fieldVisitor = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc(), null, null);
		fieldVisitor.visitEnd();

		paramDec.setSlot(slot++);

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(i);
		i++;
		mv.visitInsn(AALOAD);

		if(paramDec.getTypeName() == TypeName.INTEGER) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
		}
		else if(paramDec.getTypeName() == TypeName.BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
		}
		return null;
	}

//	@Override
//	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
//		//TODO Implement this
//		//For assignment 5, only needs to handle integers and booleans
//		FieldVisitor visit ;
//		if(paramDec.getTypeName() == TypeName.INTEGER)
//		{
//			visit = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "I", null, null);
//			visit.visitEnd();
//		}
//		else if(paramDec.getTypeName() == TypeName.BOOLEAN)
//		{
//			visit = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(), "Z", null, null);
//			visit.visitEnd();
//		}
//
//		paramDec.setSlot(slot++);
//
//		mv.visitVarInsn(ALOAD, 0);
//		mv.visitVarInsn(ALOAD, 1);
//		mv.visitLdcInsn(i);
//		i++;
//		mv.visitInsn(AALOAD);
//
//		if(paramDec.getTypeName() == TypeName.INTEGER)
//		{
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
//			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
//		}
//		else if(paramDec.getTypeName() == TypeName.BOOLEAN)
//		{
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
//			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
//		}
//
//		return null;
//	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label guardLabel = new Label();
		Label bodyLabel = new Label();
		mv.visitJumpInsn(GOTO, guardLabel);
		mv.visitLabel(bodyLabel);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(guardLabel);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, bodyLabel);
		return null;
	}
}
