package fr.unice.yamb.tests.spouts;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Map;

public class XMLSpout extends BaseRichSpout {

    private SpoutOutputCollector _collector;
    private long count;
    private long sleepTime;
    private File exampleXMLFile;
    private ArrayList<String> XMLData;


    //String[] words;

    public XMLSpout(){
        this.sleepTime = 1000;
    }

    public XMLSpout(long millis){
        this.sleepTime = millis;
    }

    public Document parseXMLFile(File inpytFile){
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inpytFile);
            doc.getDocumentElement().normalize();
            return doc;

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public String XMLToString(Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String XMLToString(Node node){
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(node), new StreamResult(sw));
            return sw.toString();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public ArrayList<String> SplitXML(Document doc){

        ArrayList<String> list = new ArrayList();
        NodeList students = doc.getElementsByTagName("student");
        for(int i=0; i<students.getLength(); i++){
            list.add(
                    XMLToString( students.item(i) )
            );
        }

        return list;

    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector){
        try {
            this.exampleXMLFile = new File("/home/sdnuser/yamb/tests/resources/example.xml");
            System.out.println("Looking for file in: " + this.exampleXMLFile);
        } catch(Exception e){
            e.printStackTrace();
        }
        this.count = 0;

        Document doc = parseXMLFile(this.exampleXMLFile);
        this.XMLData = SplitXML(doc);
        this._collector = collector;
    }

    @Override
    public void activate(){

    }

    public void nextTuple(){
        Utils.sleep(this.sleepTime);
        String nextTuple = this.XMLData.get((int)count%this.XMLData.size());
        _collector.emit(new Values(nextTuple), count++);
        // System.out.println("emitted: \n" + nextTuple);
    }

    @Override
    public void ack(Object msgId){ super.ack(msgId); }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("xml"));
    }

}
