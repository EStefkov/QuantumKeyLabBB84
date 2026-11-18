import ui.QuantumKeyLab;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import simulation.*;
import experiments.*;
@SuppressWarnings("unchecked")
public class BB84DesktopTest {
 static final Path artifacts = createArtifacts();
 static Path createArtifacts(){try{return Files.createTempDirectory("bb84-desktop-check-");}catch(Exception e){throw new RuntimeException(e);}}
 static QuantumKeyLab frame;
 static Object field(String name)throws Exception{Field f=QuantumKeyLab.class.getDeclaredField(name);f.setAccessible(true);return f.get(frame);}
 static void call(String name,Class<?>[] types,Object... args)throws Exception{Method m=QuantumKeyLab.class.getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(frame,args);}
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);}
 static void idle()throws Exception{for(int i=0;i<400;i++){boolean[] b={false};SwingUtilities.invokeAndWait(()->{try{b[0]=(Boolean)field("busy");}catch(Exception e){throw new RuntimeException(e);}});if(!b[0])return;Thread.sleep(25);}throw new AssertionError("Worker timeout");}
 static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container)layout((Container)child);}
 public static void main(String[] args)throws Exception{
 try{
 SwingUtilities.invokeAndWait(()->{frame=new QuantumKeyLab();try{call("simulate",new Class[]{boolean.class},true);}catch(Exception e){throw new RuntimeException(e);}});idle();
 SwingUtilities.invokeAndWait(()->{try{
  check((Integer)field("stage")==1,"first stage");
  DefaultTableModel model=(DefaultTableModel)field("traceModel");check(model.getRowCount()==20,"20 rows");check(model.getValueAt(0,7).equals("…"),"Bob result must be hidden initially");
  for(int i=0;i<4;i++)call("simulate",new Class[]{boolean.class},true);
  check((Integer)field("stage")==5,"last stage");
  SimulationResult result=(SimulationResult)field("result");check(result.getTrace().size()==20,"full trace");
  frame.getContentPane().setSize(1320,780);layout(frame.getContentPane());
  BufferedImage image=new BufferedImage(1320,780,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();frame.getContentPane().printAll(g);g.dispose();
  javax.imageio.ImageIO.write(image,"png",artifacts.resolve("gui-preview.png").toFile());
  ((JTextField)field("n")).setText("200");check(field("result")==null,"parameter change clears old result");
  call("batch",new Class[]{});
 }catch(Exception e){throw new RuntimeException(e);}});idle();
 SwingUtilities.invokeAndWait(()->{try{
  java.util.List<ExperimentRunRecord> rows=(java.util.List<ExperimentRunRecord>)field("records");check(rows.size()==3600,"batch rows");
  check(((DefaultTableModel)field("batchModel")).getRowCount()==36,"36 summaries");
  check(rows.stream().map(ExperimentRunRecord::getSeed).distinct().count()==3600,"unique seeds");
  SummaryExporter.write(rows,artifacts.resolve("summary.csv"));
  Object chart=field("chart");Method render=chart.getClass().getDeclaredMethod("renderImage",int.class);render.setAccessible(true);
  for(int i=0;i<3;i++)javax.imageio.ImageIO.write((BufferedImage)render.invoke(chart,i),"png",artifacts.resolve("chart-"+i+".png").toFile());
  call("emptyDemo",new Class[]{});
 }catch(Exception e){throw new RuntimeException(e);}});idle();
 SwingUtilities.invokeAndWait(()->{try{check("insufficient_data".equals(((SimulationResult)field("result")).getDecision()),"empty demo");call("preset",new Class[]{int.class,double.class,double.class,long.class},20,0.0,1.0,42L);}catch(Exception e){throw new RuntimeException(e);}});idle();
 SwingUtilities.invokeAndWait(()->{try{check(((SimulationResult)field("result")).getQberPercent()==100,"noise demo");call("tests",new Class[]{});}catch(Exception e){throw new RuntimeException(e);}});idle();
 SwingUtilities.invokeAndWait(()->{try{check(((JTextArea)field("log")).getText().contains("PASS: 21 tests"),"GUI test action");}catch(Exception e){throw new RuntimeException(e);}frame.dispose();});
 System.out.println("Artifacts: "+artifacts);
 System.out.println("PASS: Swing integration (stages, reset, batch, charts, boundary demos, test action)");
 }catch(Throwable t){t.printStackTrace();System.exit(1);}System.exit(0);
 }
}
