package kr.ac.jbnu.ssel.instantfeedback.domain;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class TreeTable extends Canvas {
	private Grid treeAndReadabilityTable;
	public static char c = 0;
	
	public TreeTable(Composite parent, int style) {
		super(parent, style);
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(org.eclipse.swt.events.PaintEvent e) {
				TreeTable.this.paintControl(e);
			}
		});
	}
	
	void paintControl(PaintEvent e)
	{
		if(TreeTable.c == 0)
		{
			TreeTable.c++;
			return;
		}
		
		treeAndReadabilityTable = new Grid(this, SWT.NONE);
		treeAndReadabilityTable.setAutoWidth(true);
		treeAndReadabilityTable.setHeaderVisible(true);
		treeAndReadabilityTable.setColumnScrolling(true);
		
		GridColumn projectTreeCol = new GridColumn(treeAndReadabilityTable, SWT.NONE);
		projectTreeCol.setAlignment(SWT.CENTER);
		projectTreeCol.setText("Project Tree");
		projectTreeCol.setWidth(100);
		projectTreeCol.setTree(true);
		
		GridColumn LOCCol = new GridColumn(treeAndReadabilityTable, SWT.NONE);
		LOCCol.setAlignment(SWT.CENTER);
		LOCCol.setText("LOC");
		LOCCol.setWidth(100);
		
		GridColumn LoopCol = new GridColumn(treeAndReadabilityTable, SWT.NONE);
		LoopCol.setAlignment(SWT.CENTER);
		LoopCol.setText("Loop");
		LoopCol.setWidth(100);
		
		GridColumn AssignmentCol = new GridColumn(treeAndReadabilityTable, SWT.NONE);
		AssignmentCol.setAlignment(SWT.CENTER);
		AssignmentCol.setText("Assignment");
		AssignmentCol.setWidth(100);
		
		GridColumn BranchCol = new GridColumn(treeAndReadabilityTable, SWT.NONE);
		BranchCol.setAlignment(SWT.CENTER);
		BranchCol.setText("Branch");
		BranchCol.setWidth(100);
		
		GridColumn ReadabilityCol = new GridColumn(treeAndReadabilityTable, SWT.NONE);
		ReadabilityCol.setAlignment(SWT.CENTER);
		ReadabilityCol.setText("Readability");
		ReadabilityCol.setWidth(100);
		
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
	}
}