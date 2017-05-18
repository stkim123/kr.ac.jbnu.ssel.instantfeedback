//package kr.ac.jbnu.ssel.instantfeedback.tool;
//
//import com.harukizaemon.simian.AuditListener;
//import com.harukizaemon.simian.Block;
//import com.harukizaemon.simian.CheckSummary;
//import com.harukizaemon.simian.Options;
//import com.harukizaemon.simian.SourceFile;
//
//public class MyAuditListener implements AuditListener {
//	private int duplicateBlockCount;
//	private int duplicateLineCount;
//
//	@Override
//	public void block(Block block) {
//	}
//
//	@Override
//	public void endCheck(CheckSummary summary) {
//		System.out.println("-----------------------summary-------------------------");
//		System.out.println("DuplicateBlockCount : " + summary.getDuplicateBlockCount());
//		System.out.println("DuplicateLineCount : " + summary.getDuplicateLineCount());
//		System.out.println("DuplicateLinePercentage : " + summary.getDuplicateLinePercentage());
//		System.out.println("TotalSignificantLineCount : " + summary.getTotalSignificantLineCount());
//		System.out.println("-------------------------------------------------------");
//		
//		duplicateBlockCount = summary.getDuplicateBlockCount();
//		duplicateLineCount = summary.getDuplicateLineCount();
//	}
//
//	@Override
//	public void endSet(String arg0) {
//	}
//
//	@Override
//	public void fileProcessed(SourceFile arg0) {
//	}
//
//	@Override
//	public void startCheck(Options arg0) {
//		System.out.println("starting check");
//	}
//
//	@Override
//	public void startSet(int arg0) {
//	}
//
//	public int getDuplicateBlockCount() {
//		return duplicateBlockCount;
//	}
//
//	public void setDuplicateBlockCount(int duplicateBlockCount) {
//		this.duplicateBlockCount = duplicateBlockCount;
//	}
//
//	public int getDuplicateLineCount() {
//		return duplicateLineCount;
//	}
//
//	public void setDuplicateLineCount(int duplicateLineCount) {
//		this.duplicateLineCount = duplicateLineCount;
//	}
//}
