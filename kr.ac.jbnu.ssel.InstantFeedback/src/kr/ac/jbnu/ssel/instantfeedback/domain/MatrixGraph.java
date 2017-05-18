package kr.ac.jbnu.ssel.instantfeedback.domain;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class MatrixGraph extends Canvas {
	private char count;
	private XYGraph matrixGraph;
	private LightweightSystem lws;

	public MatrixGraph(Composite parent, int style) {
		super(parent, style);
		addPaintListener(new PaintListener() {
			@Override
			public void paintControl(org.eclipse.swt.events.PaintEvent e) {
				MatrixGraph.this.paintControl(e);
			}
		});
	}

	public void paintControl(PaintEvent e) {
		if (count < 1)
			count++;
		else {
			lws = new LightweightSystem(this);

			matrixGraph = new XYGraph();
			
			setGraphProperties();
			
		}
	}

	public void setData(List<Readability> data) {
		if (matrixGraph != null) {
			matrixGraph.removeAll();
			
			matrixGraph = new XYGraph();
			
			matrixGraph.getPrimaryXAxis().setRange((double) data.size(), 1.0);
			matrixGraph.getPrimaryYAxis().setRange(1.0, findMaximum(data));
			
			double[] xValues = new double [data.size()];
			double[] yValues = new double [data.size()];
			for(int i=0; i<data.size(); i++)
			{
				xValues[i] = (double)(i+1);
				yValues[i] = data.get(i).getReadability();
			}
			
			CircularBufferDataProvider traceDataProvider = new CircularBufferDataProvider(false);
			traceDataProvider.setBufferSize(100);
			traceDataProvider.setCurrentXDataArray(xValues);
			traceDataProvider.setCurrentYDataArray(yValues);

			Trace trace = new Trace("Readability changes", matrixGraph.getPrimaryXAxis()
					, matrixGraph.getPrimaryYAxis(), traceDataProvider);
			
			trace.getYAxis().setVisible(false);
			trace.getXAxis().setTitle("");

			trace.setPointStyle(PointStyle.XCROSS);

			matrixGraph.addTrace(trace);
			
			setGraphProperties();

			lws.setContents(matrixGraph);
		}
	}
	
	public void testDraw()
	{
		String serverUrl = "http://localhost:8080/readability";
		
		Readability readability = new Readability();
		readability.setMethodName("testmethod1");
		readability.setClassName("testclass1");
		
		User user = new User();
		user.setUsername("test1");
		
		readability.setUser(user);
		
		List<Readability> result = null;

		try {
			String mappedData = "username=" + readability.getUser().getUsername() + "&methodName="
					+ readability.getMethodName() + "&className=" + readability.getClassName();
			// String mappedData =
			// "username=test1&methodName=testmethod1&className=testclass1";
			ObjectMapper objectMapper = new ObjectMapper();
			// String mappedData = objectMapper.writeValueAsString(readability);
			// mappedData = "readability="+mappedData;

			URL obj = new URL(serverUrl + "/graphData");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("POST");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(mappedData);
			wr.flush();
			wr.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				// Readability responseData = objectMapper.readValue(inputLine,
				// Readability.class);
				result = objectMapper.readValue(inputLine,
						TypeFactory.defaultInstance().constructCollectionType(List.class, Readability.class));
			}
			in.close();
			con.disconnect();
		} catch (JsonProcessingException JPE) {
			JPE.printStackTrace();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		setData(result);
	}
	
	private double findMaximum(List<Readability> data){
		double max = 0.0;
		Readability readability;
		for(int i=0; i<data.size(); i++)
		{
			readability =data.get(i);
			double score = readability.getReadability();
			if(max<score)
				max = score;
		}
		return max;
	}
	
	private void setGraphProperties(){
		matrixGraph.setTitle("Readability");
		matrixGraph.setFocusTraversable(false);
		matrixGraph.setShowLegend(false);
		matrixGraph.getPrimaryXAxis().setShowMajorGrid(false);
		matrixGraph.getPrimaryYAxis().setShowMajorGrid(false);
	}
}
