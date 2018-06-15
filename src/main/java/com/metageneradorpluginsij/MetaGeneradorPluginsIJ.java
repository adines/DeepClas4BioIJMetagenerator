package com.metageneradorpluginsij;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author adines
 */
public class MetaGeneradorPluginsIJ {

    public static String[] getParams() {
        String result[] = null;
        JDialog adAPId = null;
        JDialog gdd = null;
        try {

            String so = System.getProperty("os.name");
            String python;
            if (so.contains("Windows")) {
                python = "python ";
            } else {
                python = "python3 ";
            }

            GridLayout glAPI = new GridLayout(2, 1);
            JPanel apiPanel = new JPanel(glAPI);

            JTextField tf = new JTextField();
            apiPanel.add(new JLabel("Introduce the path of the API"));
            apiPanel.add(tf);

            JOptionPane adAPI = new JOptionPane(apiPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);

            adAPId = adAPI.createDialog("API path");
            adAPId.setVisible(true);
            Object selectedValue = adAPI.getValue();
            if (selectedValue instanceof Integer) {
                int selected = ((Integer) selectedValue).intValue();
                if (selected == 0) {
                    String pathAPI = tf.getText();
                    adAPId.dispose();
                    String comando = python + pathAPI + "listFrameworks.py";
                    Process p = Runtime.getRuntime().exec(comando);
                    p.waitFor();
                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("data.json"));
                    JSONArray frameworks = (JSONArray) jsonObject.get("frameworks");

                    int i = 0;
                    String opcionesFramework[] = new String[frameworks.size()];
                    for (Object o : frameworks) {
                        opcionesFramework[i] = (String) o;
                        i++;
                    }

                    JComboBox<String> frameworkChoices = new JComboBox<>(opcionesFramework);
                    JComboBox<String> modelChoices = new JComboBox<>();

                    JLabel label1 = new JLabel("Select the framework and the model");
                    JLabel lFrmaework = new JLabel("Framework: ");
                    JLabel lModel = new JLabel("Model: ");

                    GridLayout gl = new GridLayout(3, 2);
                    gl.setHgap(10);
                    gl.setVgap(10);

                    comando = python + pathAPI + "listModels.py -f Keras";
                    p = Runtime.getRuntime().exec(comando);
                    p.waitFor();
                    JSONParser parser2 = new JSONParser();
                    JSONObject jsonObject2 = (JSONObject) parser2.parse(new FileReader("data.json"));
                    JSONArray models = (JSONArray) jsonObject2.get("models");
                    modelChoices.removeAllItems();
                    for (Object o : models) {
                        modelChoices.addItem((String) o);
                    }

                    JPanel gd1 = new JPanel(gl);

                    gd1.add(label1);
                    gd1.add(new Label());
                    gd1.add(lFrmaework);
                    gd1.add(frameworkChoices);
                    gd1.add(lModel);

                    gd1.add(modelChoices);
                    gd1.repaint();

                    JOptionPane gd = new JOptionPane(gd1, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);

                    frameworkChoices.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            try {
                                String frameworkSelected = (String) frameworkChoices.getSelectedItem();
                                String comando = python + pathAPI + "listModels.py -f " + frameworkSelected;
                                Process p = Runtime.getRuntime().exec(comando);
                                p.waitFor();
                                JSONParser parser = new JSONParser();
                                JSONObject jsonObject = (JSONObject) parser.parse(new FileReader("data.json"));
                                JSONArray frameworks = (JSONArray) jsonObject.get("models");
                                modelChoices.removeAllItems();
                                for (Object o : frameworks) {
                                    modelChoices.addItem((String) o);
                                }
                                modelChoices.doLayout();

                            } catch (InterruptedException ex) {
                                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IOException ex) {
                                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (org.json.simple.parser.ParseException ex) {
                                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });

                    gdd = gd.createDialog("Select Input");
                    gdd.setVisible(true);
                    Object selectedValue2 = gd.getValue();
                    if (selectedValue2 instanceof Integer) {
                        int selected2 = ((Integer) selectedValue2).intValue();
                        if (selected2 == 0) {
                            result = new String[2];
                            result[0] = (String) frameworkChoices.getSelectedItem();
                            result[1] = (String) modelChoices.getSelectedItem();
                            gdd.dispose();
                        }
                    }

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
        } catch (org.json.simple.parser.ParseException ex) {
            Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (adAPId != null) {
                adAPId.dispose();
            }
            if (gdd != null) {
                gdd.dispose();
            }
        }
        return result;

    }

    public static void main(String[] args) {

        String arguments[] = getParams();
        if (arguments != null) {

            String framework = arguments[0];
            String model = arguments[1];
            String className = framework + model + "_IJPlugin";

            FileWriter fw = null;
            PrintWriter pw = null;

            try {
                fw = new FileWriter(className + ".java");
                pw = new PrintWriter(fw);

                //IMPORTS
                pw.println("import java.io.FileNotFoundException;");
                pw.println("import java.io.FileReader;");
                pw.println("import java.io.IOException;");
                pw.println("import java.util.logging.Level;");
                pw.println("import java.util.logging.Logger;");

                pw.println();

                pw.println("import org.scijava.command.Command;");
                pw.println("import org.scijava.plugin.Parameter;");
                pw.println("import org.scijava.plugin.Plugin;");

                pw.println();

                pw.println("import org.json.simple.JSONObject;");
                pw.println("import org.json.simple.parser.JSONParser;");
                pw.println("import org.json.simple.parser.ParseException;");

                pw.println();

                pw.println("import ij.IJ;");
                pw.println("import ij.ImagePlus;");

                pw.println();
                //COMIENZO DE LA CLASE
                pw.println("@Plugin(type = Command.class, headless = true, menuPath = \"Plugins>" + className + "\")");
                pw.println("public class " + className + " {");
                //Parametros
                pw.println("\t @Parameter");
                pw.println("\t private ImagePlus imp;");
                pw.println("\t @Parameter");
                pw.println("\t private String pathAPI;");
                pw.println();
                //METODO RUN
                pw.println("\t @Override");
                pw.println("\t public void run() {");

                pw.println("\t\t try{");
                pw.println("\t\t\t String so=System.getProperty(\"os.name\");");
                pw.println("\t\t\t String python;");

                //SISTEMA OPERATIVO
                pw.println("\t\t\t if(so.contains(\"Windows\")){");
                pw.println("\t\t\t\t  python=\"python \";");
                pw.println("\t\t\t }else{");
                pw.println("\t\t\t\t python=\"python3 \";");
                pw.println("\t\t\t }");
                pw.println();

                //NOMBRE DE LA IMAGEN
                pw.println("\t\t\t imp = IJ.getImage();");
                pw.println();
                pw.println("\t\t\t String image = IJ.getDirectory(\"image\") + imp.getTitle();");

                pw.println(); //COMANDO
                pw.println("\t\t\t String comando = python + pathAPI + \"predict.py -i \" + image + \" -f " + framework + " -m " + model + "\";");
                pw.println("\t\t\t Process p = Runtime.getRuntime().exec(comando);");
                pw.println("\t\t\t p.waitFor();");

                pw.println();
                //LEER JSON
                pw.println("\t\t\t JSONParser parser = new JSONParser();");
                pw.println("\t\t\t JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(\"data.json\"));");
                pw.println("\t\t\t String classPredict = (String) jsonObject3.get(\"class\");");
                pw.println("\t\t\t IJ.showMessage(\"Prediction\", \"The class which the image belongs is \" + classPredict);");

                pw.println("\t\t } catch (FileNotFoundException ex) {");
                pw.println("\t\t\t IJ.showMessage(\"Error\", \"You need to indicate the path of the API in the config file.\");");
                pw.println("\t\t } catch (IOException ex) {");
                pw.println("\t\t\t Logger.getLogger(" + className + ".class.getName()).log(Level.SEVERE, null, ex);");
                pw.println("\t\t } catch (InterruptedException ex) {");
                pw.println("\t\t\t Logger.getLogger(" + className + ".class.getName()).log(Level.SEVERE, null, ex);");
                pw.println("\t\t } catch (ParseException ex) {");
                pw.println("\t\t\t Logger.getLogger(" + className + ".class.getName()).log(Level.SEVERE, null, ex);");
                pw.println("\t\t }");

                pw.println("\t }"); //FIN DEL RUN

                pw.println("}"); //FIN DE LA CLASE

                //ESCRIBIMOS EL FIHCERO POM.XML
                DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                Document doc = docBuilder.newDocument();
                Element rootElement = doc.createElement("project");
                doc.appendChild(rootElement);

                Attr attrXXMLNS = doc.createAttribute("xmlns");
                attrXXMLNS.setValue("http://maven.apache.org/POM/4.0.0");
                rootElement.setAttributeNode(attrXXMLNS);

                Attr attrXSI = doc.createAttribute("xmlns:xsi");
                attrXSI.setValue("http://www.w3.org/2001/XMLSchema-instance");
                rootElement.setAttributeNode(attrXSI);

                Attr attrSchema = doc.createAttribute("xsi:schemaLocation");
                attrSchema.setValue("http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd");
                rootElement.setAttributeNode(attrSchema);

                Element modelV = doc.createElement("modelVersion");
                modelV.appendChild(doc.createTextNode("4.0.0"));
                rootElement.appendChild(modelV);

                Element parent = doc.createElement("parent");
                rootElement.appendChild(parent);

                Element groupId = doc.createElement("groupId");
                groupId.appendChild(doc.createTextNode("org.scijava"));
                parent.appendChild(groupId);

                Element artifactId = doc.createElement("artifactId");
                artifactId.appendChild(doc.createTextNode("pom-scijava"));
                parent.appendChild(artifactId);

                Element version = doc.createElement("version");
                version.appendChild(doc.createTextNode("16.1.0"));
                parent.appendChild(version);

                Element relativePath = doc.createElement("relativePath");
                parent.appendChild(relativePath);

                Element groupId2 = doc.createElement("groupId");
                groupId2.appendChild(doc.createTextNode("com"));
                rootElement.appendChild(groupId2);

                Element artifactId2 = doc.createElement("artifactId");
                artifactId2.appendChild(doc.createTextNode(className));
                rootElement.appendChild(artifactId2);

                Element version2 = doc.createElement("version");
                version2.appendChild(doc.createTextNode("1.0"));
                rootElement.appendChild(version2);

                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(className));
                rootElement.appendChild(name);

                Element description = doc.createElement("description");
                description.appendChild(doc.createTextNode("A plugin that classify an image using the " + model + "model from " + framework));
                rootElement.appendChild(description);

                Element url = doc.createElement("url");
                url.appendChild(doc.createTextNode("https://github.com/adines/" + framework + model));
                rootElement.appendChild(url);

                Element inceptionYear = doc.createElement("inceptionYear");
                inceptionYear.appendChild(doc.createTextNode(String.valueOf(Calendar.getInstance().get(Calendar.YEAR))));
                rootElement.appendChild(inceptionYear);

                Element organization = doc.createElement("organization");
                rootElement.appendChild(organization);

                Element organizationName = doc.createElement("name");
                organizationName.appendChild(doc.createTextNode("University of La Rioja"));
                organization.appendChild(organizationName);

                Element organizationURL = doc.createElement("url");
                organizationURL.appendChild(doc.createTextNode("http://www.unirioja.es/"));
                organization.appendChild(organizationURL);

                Element licenses = doc.createElement("licenses");
                rootElement.appendChild(licenses);

                Element license = doc.createElement("license");
                licenses.appendChild(license);

                Element licenseName = doc.createElement("name");
                licenseName.appendChild(doc.createTextNode("Simplified BSD License"));
                license.appendChild(licenseName);

                Element licenseDistribution = doc.createElement("distribution");
                licenseDistribution.appendChild(doc.createTextNode("repo"));
                license.appendChild(licenseDistribution);

                Element mailingLists = doc.createElement("mailingLists");
                rootElement.appendChild(mailingLists);

                Element mailingList = doc.createElement("mailingList");
                mailingLists.appendChild(mailingList);

                Element mailingName = doc.createElement("name");
                mailingName.appendChild(doc.createTextNode("ImageJ Forum"));
                mailingList.appendChild(mailingName);

                Element mailingArchive = doc.createElement("archive");
                mailingArchive.appendChild(doc.createTextNode("http://forum.imagej.net/"));
                mailingList.appendChild(mailingArchive);

                Element scm = doc.createElement("scm");
                rootElement.appendChild(scm);

                Element connection = doc.createElement("connection");
                connection.appendChild(doc.createTextNode("scm:git:git@github.com:adines/" + className + ".git"));
                scm.appendChild(connection);

                Element developerConnection = doc.createElement("developerConnection");
                developerConnection.appendChild(doc.createTextNode("scm:git:git@github.com:adines/" + className + ".git"));
                scm.appendChild(developerConnection);

                Element tag = doc.createElement("tag");
                tag.appendChild(doc.createTextNode("HEAD"));
                scm.appendChild(tag);

                Element connectionUrl = doc.createElement("url");
                connectionUrl.appendChild(doc.createTextNode("git@github.com:adines/" + className + ".git"));
                scm.appendChild(connectionUrl);

                Element issueManagement = doc.createElement("issueManagement");
                rootElement.appendChild(issueManagement);

                Element system = doc.createElement("system");
                system.appendChild(doc.createTextNode("GitHub Issues"));
                issueManagement.appendChild(system);

                Element issueUrl = doc.createElement("url");
                issueUrl.appendChild(doc.createTextNode("https://github.com/adines/" + className + "/issues"));
                issueManagement.appendChild(issueUrl);

                Element ciManagement = doc.createElement("ciManagement");
                rootElement.appendChild(ciManagement);

                Element cisystem = doc.createElement("system");
                cisystem.appendChild(doc.createTextNode("None"));
                ciManagement.appendChild(cisystem);

                Element properties = doc.createElement("properties");
                rootElement.appendChild(properties);

                Element mainClass = doc.createElement("main-class");
                mainClass.appendChild(doc.createTextNode(className));
                properties.appendChild(mainClass);

                Element licenseLicense = doc.createElement("license.licenseName");
                licenseLicense.appendChild(doc.createTextNode("bsd_2"));
                properties.appendChild(licenseLicense);

                Element licenseOwners = doc.createElement("license.copyrightOwners");
                licenseOwners.appendChild(doc.createTextNode("University of La Rioja"));
                properties.appendChild(licenseOwners);

                Element projectEncoding = doc.createElement("project.build.sourceEncoding");
                projectEncoding.appendChild(doc.createTextNode("UTF-8"));
                properties.appendChild(projectEncoding);

                Element mavenSource = doc.createElement("maven.compiler.source");
                mavenSource.appendChild(doc.createTextNode("1.8"));
                properties.appendChild(mavenSource);

                Element mavenTarget = doc.createElement("maven.compiler.target");
                mavenTarget.appendChild(doc.createTextNode("1.8"));
                properties.appendChild(mavenTarget);

                Element jvmVersion = doc.createElement("scijava.jvm.version");
                jvmVersion.appendChild(doc.createTextNode("1.8"));
                properties.appendChild(jvmVersion);

                Element developers = doc.createElement("developers");
                rootElement.appendChild(developers);

                Element developer = doc.createElement("developer");
                developers.appendChild(developer);

                Element developerId = doc.createElement("id");
                developerId.appendChild(doc.createTextNode("adines"));
                developer.appendChild(developerId);

                Element developerName = doc.createElement("name");
                developerName.appendChild(doc.createTextNode("Adrián Inés"));
                developer.appendChild(developerName);

                Element developerUrl = doc.createElement("url");
                developerUrl.appendChild(doc.createTextNode("http://www.unirioja.es/cu/adines"));
                developer.appendChild(developerUrl);

                Element developerRoles = doc.createElement("roles");
                developer.appendChild(developerRoles);

                Element role1 = doc.createElement("role");
                role1.appendChild(doc.createTextNode("lead"));
                developerRoles.appendChild(role1);

                Element role2 = doc.createElement("role");
                role2.appendChild(doc.createTextNode("developer"));
                developerRoles.appendChild(role2);

                Element role3 = doc.createElement("role");
                role3.appendChild(doc.createTextNode("debugger"));
                developerRoles.appendChild(role3);

                Element role4 = doc.createElement("role");
                role4.appendChild(doc.createTextNode("reviewer"));
                developerRoles.appendChild(role4);

                Element role5 = doc.createElement("role");
                role5.appendChild(doc.createTextNode("support"));
                developerRoles.appendChild(role5);

                Element role6 = doc.createElement("role");
                role6.appendChild(doc.createTextNode("maintainer"));
                developerRoles.appendChild(role6);

                Element contributors = doc.createElement("contributors");
                rootElement.appendChild(contributors);

                Element contributor = doc.createElement("contributor");
                contributors.appendChild(contributor);

                Element contributorName = doc.createElement("name");
                contributorName.appendChild(doc.createTextNode("None"));
                contributor.appendChild(contributorName);

                Element repositories = doc.createElement("repositories");
                rootElement.appendChild(repositories);

                Element repository = doc.createElement("repository");
                repositories.appendChild(repository);

                Element repositoryId = doc.createElement("id");
                repositoryId.appendChild(doc.createTextNode("imagej.public"));
                repository.appendChild(repositoryId);

                Element repositoryUrl = doc.createElement("url");
                repositoryUrl.appendChild(doc.createTextNode("http://maven.imagej.net/content/groups/public"));
                repository.appendChild(repositoryUrl);

                Element dependencies = doc.createElement("dependencies");
                rootElement.appendChild(dependencies);

                Element dependency = doc.createElement("dependency");
                dependencies.appendChild(dependency);

                Element dependencyGroupId = doc.createElement("groupId");
                dependencyGroupId.appendChild(doc.createTextNode("net.imagej"));
                dependency.appendChild(dependencyGroupId);

                Element dependencyArtifactId = doc.createElement("artifactId");
                dependencyArtifactId.appendChild(doc.createTextNode("imagej"));
                dependency.appendChild(dependencyArtifactId);

                Element dependency2 = doc.createElement("dependency");
                dependencies.appendChild(dependency2);

                Element dependency2GroupId = doc.createElement("groupId");
                dependency2GroupId.appendChild(doc.createTextNode("com.googlecode.json-simple"));
                dependency2.appendChild(dependency2GroupId);

                Element dependency2ArtifactId = doc.createElement("artifactId");
                dependency2ArtifactId.appendChild(doc.createTextNode("json-simple"));
                dependency2.appendChild(dependency2ArtifactId);

                Element dependency2Version = doc.createElement("version");
                dependency2Version.appendChild(doc.createTextNode("1.1.1"));
                dependency2.appendChild(dependency2Version);

                Element dependency2Type = doc.createElement("type");
                dependency2Type.appendChild(doc.createTextNode("jar"));
                dependency2.appendChild(dependency2Type);

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(new File(className + "_pom.xml"));
                transformer.transform(source, result);

            } catch (IOException ex) {
                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException ex) {
                        Logger.getLogger(MetaGeneradorPluginsIJ.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }
    }

}
