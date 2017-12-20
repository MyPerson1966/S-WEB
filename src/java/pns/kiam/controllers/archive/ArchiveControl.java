/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pns.kiam.controllers.archive;

import java.io.File;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import pns.kiam.commonserrvice.RemoverDuplicates;
import pns.kiam.sweb.controllers.app.XXParserSWEB;
import pns.kiam.sweb.controllers.archvedb.ArchiveFileController;

/**
 *
 * @author User
 */
@Named
@SessionScoped
public class ArchiveControl extends ArchiveFileController {

    @EJB
    private XXParserSWEB xxparser;
    @EJB
    private RemoverDuplicates removerDuplicates;

    /**
     * Creates a new instance of ArchiveControl
     */
    public ArchiveControl() {
    }

    /**
     * Generates an archive of uploaded file's content.
     * <br />
     * At first removes dubbed and then puts the file's content into the
     * database
     * <br />
     * The archived files are removing from the hard
     *
     * @return
     * @throws Exception
     */
    public String createArchiveREC() {
        System.out.println("--------------------- Creating Archiv/ Step 1: Removing dobbled ---------------" + new Date());
        System.out.println("  xxparser.getArchivePath() " + xxparser.getArchivePath() + "    xxparser.getMaxDaysFileLive() " + xxparser.getMaxDaysFileLive());

        long ts = System.currentTimeMillis();
        removerDuplicates.setFileAgeInDays(40);
        removerDuplicates.setRootDir(xxparser.getArchivePath());
        removerDuplicates.removeDupleFiles();
        long te = System.currentTimeMillis();
        System.out.println("    " + (te - ts) + " ms");
        List<File> fl = removerDuplicates.getFileList();

        System.out.println("");
        System.out.println("--------------------- Creating Archiv/ Step 2: Collect the files to Archive ---------------" + new Date());
        System.out.println("  Number of  Files To Collect :  " + fl.size());

//        String rroot = fuc.getRooot() + "/satdata";
//        System.out.println("  Go across  " + rroot + " and collects existing file ");
//        System.out.println(new Date());
//
//        fmc.setArchPath(rroot);
//        Set<FileMeasured> fml = fmc.readArchiveFileDir();
//
//        System.out.println("  Found " + fml.size() + "  files");
//
//        int k = 0;
//        for (Iterator<FileMeasured> it = fml.iterator(); it.hasNext();) {
//            String tmpFName = rroot + "/";
//            FileMeasured tmpf = it.next();
//
//            if (!em.contains(tmpf)) {
//
//                String fileMonth = tmpf.getMonth() + "";
//                if (tmpf.getMonth() < 10) {
//                    fileMonth = "0" + tmpf.getMonth();
//                }
//                String fileDate = tmpf.getDate() + "";
//                if (tmpf.getDate() < 10) {
//                    fileDate = "0" + tmpf.getDate();
//                }
//                tmpFName += tmpf.getYear() + "/" + fileMonth + "-" + fileDate + "/" + tmpf.getFileName();
//                System.out.println("=======> Operation No " + k);
//
//                System.out.println(" Working with file " + tmpFName);
//                System.out.println(" File content size " + (1 + tmpf.getContent().length() / 1024) + " kB ");
//                System.out.println("  ************  ");
//                System.out.println("  IN DB   " + emA.contains(tmpf));
//                System.out.println("  ************  ");
//
//                try {
//                    System.out.println("--------------------- Creating Archiv / Step 3: Adding file to Archive ---------------" + new Date());
//                    em.getTransaction().begin();
//                    em.persist(tmpf);
//                    em.getTransaction().commit();
//                    k++;
//                } catch (PersistenceException e) {
//                    System.out.println(new Date() + "  The record with hash " + tmpf.getIntHash() + "  already exists. This operation have been crashed.   ");
//                }
//                File f = new File(tmpFName);
//                boolean ex = f.exists();
//                System.out.println(" Exist File  " + tmpFName + " -- Result:   " + ex);
//                boolean del = f.delete();
//                System.out.println(" Delete File  " + tmpFName + " -- Result:   " + del);
//            }
//        }
//        System.out.println("  Number of Archive Operations:   " + k);
//        fml.clear();
//        initial();
        return "/index.xhtml?redirect=true";
    }
}
