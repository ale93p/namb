package fr.unice.yamb.experiments.cpuload.bolts;

import org.apache.storm.tuple.Tuple;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

public class TransformationBolt extends BaseNamedBolt {

    @Override
    public String name() {
        return "transformation_bolt";
    }

    @Override
    public String runTask(Tuple tuple) {

        String xml = tuple.getString(0);
        String mark = null;
        try {
            Document doc =
                    DocumentBuilderFactory.newInstance()                // factory
                    .newDocumentBuilder()                               // builder
                    .parse(
                            new InputSource(new StringReader(xml))      // input source
                    );
            doc.normalize();

            mark = doc.getElementsByTagName("marks").item(0).getTextContent();

        } catch (Exception e){
            e.printStackTrace();
        }

        return mark;
    }

}
