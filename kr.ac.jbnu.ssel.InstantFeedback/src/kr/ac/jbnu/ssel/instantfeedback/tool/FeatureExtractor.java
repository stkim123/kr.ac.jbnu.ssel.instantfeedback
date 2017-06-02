package kr.ac.jbnu.ssel.instantfeedback.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import kr.ac.jbnu.ssel.instantfeedback.Constants;
import kr.ac.jbnu.ssel.instantfeedback.domain.Features;
import kr.ac.jbnu.ssel.instantfeedback.tool.Xmeans.Cluster;

public class FeatureExtractor {
	
	private Features features;
	private int nestedControl = 0;
	private List<Integer> nestedControlList = new ArrayList<Integer>();
	private IdentifierParser identifierParser;

	public Features extractFeatures(String methodBody) {
		features = new Features();
		identifierParser = new IdentifierParser();

		String sourceAsString = "public class AA{ " + methodBody.substring(0, methodBody.length() - 1) + "}";
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(sourceAsString.toCharArray());
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		String[] splittedSource = methodBody.split("\n");
		features.setLOC(splittedSource.length);
		features.setAllLines(Arrays.asList(splittedSource));
		StringBuilder separatedMethodBody = new StringBuilder("");

		for (String line : splittedSource) {
			Pattern escapePattern = Pattern.compile("[\\t\\r\\s]");
			Matcher escapeMatcher = escapePattern.matcher(line);
			Pattern characterPattern = Pattern.compile("[a-zA-Z{}]");
			Matcher characterMatcher = characterPattern.matcher(line);
			Pattern commentPattern = Pattern.compile("^[//]");
			Matcher commentMatcher = commentPattern.matcher(line);
			
			if(commentMatcher.find()){
				continue;
			}
			else if (line.equals("") || (escapeMatcher.find() && !characterMatcher.find())) {
				features.increaseNumOfBlankLines();
				
				if(separatedMethodBody.toString().equals(""))
					continue;
				
				features.getSeparatedStrings().add(separatedMethodBody.toString());
				separatedMethodBody = new StringBuilder();
				
			}
			else {
				separatedMethodBody.append(line+"\n");
			}
		}
		String remain = separatedMethodBody.toString();
		if(remain != null && !remain.equals("")){
			features.getSeparatedStrings().add(separatedMethodBody.toString());
		}

		features.setNumOfComments(cu.getCommentList().size());

		cu.accept(new ASTVisitor() {
			@Override
			public boolean visit(IfStatement node) {
				features.increaseNumOfBranch();

				exploreInnerStatements(node);

				features.setMaxNestedControl(getMax());
				nestedControl = 0;
				nestedControlList.clear();
				return super.visit(node);
			}

			@Override
			public boolean visit(SwitchStatement node) {
				features.increaseNumOfBranch();

				exploreInnerStatements(node);

				features.setMaxNestedControl(getMax());
				nestedControl = 0;
				nestedControlList.clear();
				return super.visit(node);
			}

			@Override
			public boolean visit(WhileStatement node) {
				features.increaseNumOfLoops();

				exploreInnerStatements(node);

				features.setMaxNestedControl(getMax());
				nestedControl = 0;
				nestedControlList.clear();
				return super.visit(node);
			}

			@Override
			public boolean visit(ForStatement node) {
				features.increaseNumOfLoops();

				exploreInnerStatements(node);

				features.setMaxNestedControl(getMax());
				nestedControl = 0;
				nestedControlList.clear();
				return super.visit(node);
			}

			@Override
			public boolean visit(EnhancedForStatement node) {
				features.increaseNumOfLoops();

				exploreInnerStatements(node);

				features.setMaxNestedControl(getMax());
				nestedControl = 0;
				nestedControlList.clear();
				return super.visit(node);
			}

			@Override
			public boolean visit(DoStatement node) {
				features.increaseNumOfLoops();

				exploreInnerStatements(node);

				features.setMaxNestedControl(getMax());
				nestedControl = 0;
				nestedControlList.clear();
				return super.visit(node);
			}

			@Override
			public boolean visit(Assignment node) {
				features.increaseNumOfAssignment();
				Operator operator = node.getOperator();
				features.addOperators(operator.toString());

				String leftOperand = node.getLeftHandSide().toString();
				List splittedIdentifier = identifierParser.parseIdentifier(leftOperand);
				features.getOperands().addAll(splittedIdentifier);

				String rightOperand = node.getRightHandSide().toString();
				if (rightOperand != null) {
					splittedIdentifier = identifierParser.parseIdentifier(rightOperand);
					features.getOperands().addAll(splittedIdentifier);
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(VariableDeclarationFragment node) {
				Expression expression = node.getInitializer();
				if (expression != null) {
					features.increaseNumOfAssignment();
					features.addOperators("=");

					String leftOperand = node.getName().getIdentifier();
					List splittedIdentifier = identifierParser.parseIdentifier(leftOperand);
					features.getOperands().addAll(splittedIdentifier);

					String rightOperand = (String) expression.getProperty("NAME");
					if (rightOperand != null) {
						splittedIdentifier = identifierParser.parseIdentifier(rightOperand);
						features.getOperands().addAll(splittedIdentifier);
					}

				}
				return super.visit(node);
			}
			
			@Override
			public boolean visit(SimpleName node) {
				String identifier = node.getIdentifier();
				List splittedIdentifier = identifierParser.parseIdentifier(identifier);
				features.getAllWords().addAll(splittedIdentifier);
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				features.setMethodName(node.getName().toString());
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodInvocation node) {
				features.increaseNumOfMethodInvocation();
				return super.visit(node);
			}

			@Override
			public boolean visit(InfixExpression node) {
				org.eclipse.jdt.core.dom.InfixExpression.Operator operator = node.getOperator();
				String operatorString = operator.toString();
				features.addOperators(operatorString);

				if (isArithmaticOperator(operatorString))
					features.increaseNumOfArithmaticOperators();

				if (isLogicalOperator(operatorString))
					features.increaseNumOfLogicalOperators();

				if (isBitOperator(operatorString))
					features.increaseNumOfBitOperators();

				String leftOperand = node.getLeftOperand().toString();
				List splittedIdentifier = identifierParser.parseIdentifier(leftOperand);
				features.getOperands().addAll(splittedIdentifier);

				String rightOperand = node.getRightOperand().toString();
				if (rightOperand != null) {
					splittedIdentifier = identifierParser.parseIdentifier(rightOperand);
					features.getOperands().addAll(splittedIdentifier);
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(PostfixExpression node) {
				org.eclipse.jdt.core.dom.PostfixExpression.Operator operator = node.getOperator();
				String operatorString = operator.toString();
				features.addOperators(operatorString);

				if (isArithmaticOperator(operatorString))
					features.increaseNumOfArithmaticOperators();

				if (isLogicalOperator(operatorString))
					features.increaseNumOfLogicalOperators();

				if (isBitOperator(operatorString))
					features.increaseNumOfBitOperators();

				return super.visit(node);
			}

			@Override
			public boolean visit(StringLiteral node) {
				features.increaseNumOfStringLiteral();
				return super.visit(node);
			}

			@Override
			public boolean visit(LineComment node) {
				features.increaseNumOfCommnents();
				return super.visit(node);
			}

			@Override
			public boolean visit(BlockComment node) {
				features.increaseNumOfCommnents();
				return super.visit(node);
			}
		});
		features.calculateProgramVolume();
		features.calculateEntropy();
		features.calcAverageOfVariableNames();
		features.calcAverageLineLength();

//		analysisDuplication(sourceAsString);
//		analysisPatterns();
		return features;
	}

	private boolean isArithmaticOperator(String operatorString) {
		if (operatorString.equals("+") || operatorString.equals("-") || operatorString.equals("*")
				|| operatorString.equals("/") || operatorString.equals("%") || operatorString.equals("++")
				|| operatorString.equals("--"))
			return true;
		return false;
	}

	private boolean isLogicalOperator(String operatorString) {
		if (operatorString.equals("&&") || operatorString.equals("||") || operatorString.equals("!"))
			return true;
		return false;
	}

	private boolean isBitOperator(String operatorString) {
		if (operatorString.equals("&") || operatorString.equals("|") || operatorString.equals("^")
				|| operatorString.equals("~") || operatorString.equals("<<") || operatorString.equals(">>")
				|| operatorString.equals(">>>"))
			return true;
		return false;
	}

	private void exploreInnerStatements(Object obj) {
		int previousNestedControl = nestedControl;

		if (obj instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) obj;
			nestedControl++;

			ASTNode parentNode = ifStatement.getParent();
			if (parentNode.getNodeType() == ASTNode.IF_STATEMENT)
				nestedControl--;

			Object then = ifStatement.getThenStatement();
			if ((then != null) && isStatement(then)) {
				exploreInnerStatements(then);
			} else if (isBlock(then)) {
				Block thenBlock = (Block) ifStatement.getThenStatement();
				for (Object o : thenBlock.statements()) {
					exploreInnerStatements(o);
					nestedControlList.add(nestedControl);
					nestedControl = previousNestedControl + 1;
				}
			}

			nestedControl--;
			Object elseStatement = ifStatement.getElseStatement();
			if (isStatement(elseStatement))
				exploreInnerStatements(elseStatement);
			else if (isBlock(elseStatement)) {
				nestedControl++;
				Block elseBlock = (Block) elseStatement;
				for (Object o : elseBlock.statements()) {
					exploreInnerStatements(o);
					nestedControlList.add(nestedControl);
					nestedControl = previousNestedControl + 1;
				}
			}

			if (nestedControl != previousNestedControl)
				nestedControl = previousNestedControl;
		} else if (obj instanceof SwitchStatement) {
			nestedControl = previousNestedControl;
			nestedControl++;

			SwitchStatement switchStatement = (SwitchStatement) obj;
			for (Object o : switchStatement.statements()) {
				exploreInnerStatements(o);
				nestedControlList.add(nestedControl);
				nestedControl = previousNestedControl + 1;
			}

			if (nestedControl != previousNestedControl)
				nestedControl = previousNestedControl;
		} else if (obj instanceof DoStatement) {
			nestedControl = previousNestedControl;
			nestedControl++;

			DoStatement doStatement = (DoStatement) obj;
			Object body = doStatement.getBody();
			if ((body != null) && isStatement(body))
				exploreInnerStatements(body);
			else if (isBlock(body)) {
				Block bodyBlock = (Block) body;
				for (Object o : bodyBlock.statements()) {
					exploreInnerStatements(o);
					nestedControlList.add(nestedControl);
					nestedControl = previousNestedControl + 1;
				}
			}

			if (nestedControl != previousNestedControl)
				nestedControl = previousNestedControl;
		} else if (obj instanceof ForStatement) {
			nestedControl = previousNestedControl;
			nestedControl++;

			ForStatement forStatement = (ForStatement) obj;
			Object body = forStatement.getBody();
			if ((body != null) && isStatement(body))
				exploreInnerStatements(body);
			else if (isBlock(body)) {
				Block bodyBlock = (Block) body;
				for (Object o : bodyBlock.statements()) {
					exploreInnerStatements(o);
					nestedControlList.add(nestedControl);
					nestedControl = previousNestedControl + 1;
				}
			}

			if (nestedControl != previousNestedControl)
				nestedControl = previousNestedControl;
		} else if (obj instanceof EnhancedForStatement) {
			nestedControl = previousNestedControl;
			nestedControl++;

			EnhancedForStatement forStatement = (EnhancedForStatement) obj;
			Object body = forStatement.getBody();
			if ((body != null) && isStatement(body))
				exploreInnerStatements(body);
			else if (isBlock(body)) {
				Block bodyBlock = (Block) body;
				for (Object o : bodyBlock.statements()) {
					exploreInnerStatements(o);
					nestedControlList.add(nestedControl);
					nestedControl = previousNestedControl + 1;
				}
			}

			if (nestedControl != previousNestedControl)
				nestedControl = previousNestedControl;
		} else if (obj instanceof WhileStatement) {
			nestedControl = previousNestedControl;
			nestedControl++;

			WhileStatement whileStatement = (WhileStatement) obj;

			Object body = whileStatement.getBody();
			if ((body != null) && isStatement(body))
				exploreInnerStatements(body);
			else if (isBlock(body)) {
				Block bodyBlock = (Block) body;
				for (Object o : bodyBlock.statements()) {
					exploreInnerStatements(o);
					nestedControlList.add(nestedControl);
					nestedControl = previousNestedControl + 1;
				}
			}

			if (nestedControl != previousNestedControl)
				nestedControl = previousNestedControl;
		}
	}

	private boolean isStatement(Object statements) {
		if (statements == null)
			return false;

		if (statements instanceof Block)
			return false;

		if (statements instanceof Statement) {
			return true;
		} else
			return false;
	}

	private boolean isBlock(Object statement) {
		if (statement instanceof Block) {
			return true;
		} else
			return false;
	}

	private int getMax() {
		int max = 0;
		for (int value : nestedControlList) {
			if (max < value)
				max = value;
		}
		return max;
	}

//	private void analysisDuplication(String classAsString) {
//		MyAuditListener auditListener = new MyAuditListener();
//
//		simianFile(classAsString);
//
//		Options options = new Options();
//		options.setThreshold(6);
//		options.setOption(Option.IGNORE_STRINGS, true);
//
//		Checker checker = new Checker(auditListener, options);
//
//		StreamLoader streamLoader = new StreamLoader(checker);
//
//		FileLoader fileLoader = new FileLoader(streamLoader);
//
//		try {
//			fileLoader.load(tempFile);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		if (checker.check()) {
//			features.setNumOfDuplicatedBlocks(auditListener.getDuplicateBlockCount());
//			features.setNumOfDuplicatedLines(auditListener.getDuplicateLineCount());
//		}
//		tempFile.delete();
//	}

//	private void simianFile(String classString) {
//		// IWorkbenchPage page =
//		// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		//
//		// IWorkbenchPart activePart = page.getActivePart();
//		//
//		// if (activePart instanceof IEditorPart)
//		// {
//		// IEditorInput input = ((IEditorPart)activePart).getEditorInput();
//		//
//		// if (input instanceof IFileEditorInput)
//		// {
//		// IFile file = ((IFileEditorInput)input).getFile();
//		// String filePath = file.getLocation().toOSString();
//		// String[] spplittedPath = filePath.split("\\\\");
//		// spplittedPath[spplittedPath.length-1] = temporaryFileName;
//		//
//		// StringBuilder builder = new StringBuilder();
//		//
//		// for (String splitPath : spplittedPath) {
//		// if(builder.length() == 0){
//		// builder.append(splitPath);
//		// continue;
//		// }
//		//
//		// builder.append("\\"+splitPath);
//		// }
//		//
//		// tempFile = new File(builder.toString());
//		// }
//		// }
//
//		tempFile = new File(Constants.simianPath);
//
//		try {
//			if (!tempFile.exists())
//				tempFile.createNewFile();
//
//			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
//
//			bw.write(classString);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private void analysisPatterns() {
		features.buildTermFrequencyMatrics();
		
		Cluster clusterer = new Cluster();
		clusterer.setDataset(features.getTermFrequencyMatrics());
		clusterer.doXmeans();
		int sameLineNum = clusterer.getSamePatternLines();
		features.calculatePatternRate(sameLineNum);
	}
}
