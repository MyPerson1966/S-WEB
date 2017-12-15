/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pns.kiam.sweb.controllers.telescope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import org.jboss.weld.interceptor.proxy.InterceptorException;
import org.primefaces.model.UploadedFile;
import pns.kiam.entities.telescopes.Telescope;
import pns.kiam.entities.telescopes.TelescopeHorizontMask;
import pns.kiam.sweb.controllers.AbstractController;

/**
 *
 * @author PSEVO tochka
 */
@Stateless
@Named
public class TelescopeListUploaderController extends AbstractController {

    @EJB
    private TelescopeController telescopeController;

    private String fileContent;
    private UploadedFile file;
    private List<String> csvRecords = new ArrayList<>();

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public void upload() throws IOException {
        if (file != null) {
            System.out.println(" File: " + file.getFileName());
            String line = "";
            try (BufferedReader bir
                    = new BufferedReader(
                            new InputStreamReader(file.getInputstream())
                    )) {
                line = bir.readLine();
                while ((line = bir.readLine()) != null) {
                    count++;
                    line = line.trim();
                    if (line.split(";").length > 0) {
                        byte[] b = line.getBytes();
                        line = new String(b, "UTF-8");
////			System.out.println(count + "::   " + line);
                        //if (count < 120) {
                        System.out.println(line);
                        System.out.println(line.split(";").length);
                        telescopeFromProp(line.split(";"));
                        //}
                    }
                }

            }
        }
//        for (int k = 0; k < telescopeController.getTelescopeList().size(); k++) {
//            Telescope t = telescopeController.getTelescopeList().get(k);
//            saveToDB(t);
//
//        }
    }

    int count = 0;

    private void telescopeFromProp(String[] prop) {
        if (prop.length != 36) {
            return;
        }
        System.out.println("**");

        System.out.println(count + "                               ");
        System.out.println("***********************");
        System.out.println("");
        Telescope ts = new Telescope();
        TelescopeHorizontMask tr;

        long identifier = (System.currentTimeMillis() / 500 + (int) (222 * Math.random())) % 50001;
        try {
            identifier = Long.parseLong(prop[2].trim());
        } catch (NumberFormatException e) {
        }

        if (identifier == 0) {
            identifier = (long) (50000 * Math.random());
            try {
                identifier = Long.parseLong("-1000" + identifier);
            } catch (NumberFormatException e) {
            }
        }
        double x = 0, y = 0, z = 0, height = 0, latitude = 0, longitude = 0;
        latitude = strToDouble(prop[3]);
        longitude = strToDouble(prop[4]);

        x = strToDouble(prop[8]);
        y = strToDouble(prop[9]);
        z = strToDouble(prop[10]);
        height = strToDouble(prop[5]);

        System.out.println("****************************************identifier " + identifier);

        /**
         * Converting the Angles Data to standard String
         */
        System.out.println(" propLen " + prop.length);
        for (int k = 11; k < prop.length; k++) {

            //System.out.println("    " + tn + "  -------------->> tAng " + tAng);
            try {
                double hor = (15 * (k - 11));
                if (hor <= 360) {// horizont can not be greater then 360 step of formating horizont = 15
                    tr = new TelescopeHorizontMask();

                    double ang = 0;
                    ang = strToDouble(prop[k]);
                    tr.setHorizont(hor);
                    tr.setAngle(ang);
//		    System.out.println(k + ":   ang " + ang + "        ***  prop[k] " + prop[k] + "       *** len " + prop[k].length());
//		    System.out.println(k + ":  Mask " + tr);

                    ts.getTelescopeMask().add(tr);
                    em.persist(tr);
                }
            } catch (NumberFormatException e) {
            }
            //
        }

        ts.setName(prop[0]);
        ts.setLocation(prop[1]);
        ts.setHeight(height);
        ts.setIdentifier(identifier);
        ts.setLongitude(longitude);
        ts.setLatitude(latitude);
        ts.setX(x);
        ts.setY(y);
        ts.setZ(z);

        System.out.println(
                count + "       Telescope  :  " + ts.toString());
        System.out.println(System.lineSeparator() + "     TelescopeMask: " + ts.getTelescopeMask() + System.lineSeparator() + System.lineSeparator());

        telescopeController.getTelescopeList().add(ts);
        em.persist(ts);
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(TelescopeListUploaderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveToDB(Telescope t) {
        if (t != null) {
            em.persist(t);
        }

        try {
            t = null;
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            Logger.getLogger(TelescopeListUploaderController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private double strToDouble(String s) {

        s = s.trim();
        s = s.replace(',', '.');
        if (s.equals("NaN")) {
            return 0;
        }
        if (s.length() == 0) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.csvRecords);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TelescopeListUploaderController other = (TelescopeListUploaderController) obj;
        return true;
    }

}
