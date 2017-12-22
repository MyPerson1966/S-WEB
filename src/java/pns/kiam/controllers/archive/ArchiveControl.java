package pns.kiam.controllers.archive;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import pns.fileUtils.DirectoryDeepGo;
import pns.fileUtils.FileSpecActor;
import pns.kiam.Utils.FormatClassificator;
import pns.kiam.commonserrvice.RemoverDuplicates;
import pns.kiam.entities.satellites.FileMeasured;
import pns.kiam.filecontrol.FileMeasuredController;

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
    @EJB
    private FileMeasuredController fmc;

    private FileSpecActor fsa = new FileSpecActor();
    private DirectoryDeepGo ddg = new DirectoryDeepGo();

    protected EntityManagerFactory emfA = Persistence.createEntityManagerFactory("S-WEBPU2");
    //@PersistenceContext
    protected EntityManager emA = emfA.createEntityManager();

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

//        Set<FileMeasured> fml = fileListToFileMeasuredSet(fl);
        List<FileMeasured> fml = fileListToFileMeasuredSet(fl);
        System.out.println("   fml.size()   " + fml.size());
        insertFilesToArchive(fml);
//        insertFilesToDB(fml);

        return "/archive/archivedata.xhtml?redirect=true";
    }

    private void insertFilesToDB(Set<FileMeasured> fml) {
        System.out.println(" Saving Files To Archive DB: Total " + fml.size() + " files .....");
        int k = 0;
        for (Iterator<FileMeasured> it = fml.iterator(); it.hasNext();) {
            //String tmpFName = rroot + "/";

            FileMeasured tmpf = it.next();
            if (!em.contains(tmpf)) {
                System.out.println(new Date() + "      --------------------- Creating Archiv / Step 3: Adding "
                        + " file" + tmpf.getFileName()
                        + " to Archive ---------------");
                try {
                    em.persist(tmpf);
                    k++;
                } catch (PersistenceException e) {
                    System.out.println(new Date() + "  The record with hash " + tmpf.getIntHash() + "  already exists. This operation have been crashed. "
                            + " Filename:   " + tmpf.getFileName()
                    );
                }
//                System.out.println(k + "    " + tmpf);
            }
        }
        System.out.println("  Number of Archive Operations:   " + k);
    }

    /**
     * Creating a Set of Measured Files
     *
     * @param fl
     * @return
     */
    private List<FileMeasured> fileListToFileMeasuredSet(List<File> fl) {
        //private Set<FileMeasured> fileListToFileMeasuredSet(List<File> fl) {
        int k = 0;
//        Set<FileMeasured> fml = null;
//        fml = new HashSet<>();

        List<FileMeasured> fml = null;
        fml = new ArrayList<>();

        for (File f : fl) {
            long mm = f.lastModified();
            String tmp = f.getAbsolutePath();
            tmp = tmp.replace('\\', '/');
            String[] pathPropers = tmp.split(xxparser.getArchivePath());
            System.out.println(tmp + "      pathPropers.length   " + pathPropers.length);
            String[] pathParts = pathPropers[1].split("/");
            //System.out.println("           +pathParts.length " + pathParts.length);
            String YYYY = pathParts[1];
            String DDDD = pathParts[2];
            String fileName = pathParts[pathParts.length - 1];
            System.out.println("YYYY:  " + YYYY + "     DDDD:  " + DDDD + "    fileName " + fileName);
            if (fsa.fileRead(tmp)) {
                String c = fsa.getFileContent().trim();
//                System.out.println("   content " + c);
                System.out.println("                        YYYY: " + YYYY);
                int y = gettingIntFromSTR(YYYY);
                int m = gettingIntFromSTR(DDDD.split("-")[0]);
                int d = gettingIntFromSTR(DDDD.split("-")[1]);
                System.out.println((new Date()) + " ;   file Modified     " + new Date(mm));

                FileMeasured fm = new FileMeasured();
                fm.setContent(c);

                fm.setFields(y, m, d, c, fileName, mm);
//                System.out.println("                    ******************--------------------------------->>>>>>         fm.getFileType()  " + fm.getFileType());

                fml.add(fm);
//                System.out.println(k + " c length  " + c.length());
//                System.out.println(k + " y " + fm.getYear());
//                System.out.println(k + " m " + fm.getMonth());
//                System.out.println(k + " d " + fm.getDate());
//                System.out.println(k + " c " + fm.getContent());
//
            }
            k++;
        }

        return fml;
    }

    public Set<FileMeasured> readArchiveFileDir() {

        Set<FileMeasured> fml = null;
        fml = new HashSet<>();

//        ddg.setRootDir(xxparser.getArchivePath() );
        ddg.setDirToInvestigate(xxparser.getArchivePath());
        ddg.getFileList().clear();

        ddg.goDeep();
        System.out.println("  ddg.getDirToInvestigate()   " + ddg.getDirToInvestigate() + "  ddg.getSubDirList().size()  " + ddg.getSubDirList().size());

        for (int k = 0; k < ddg.getFileList().size(); k++) {
            File f = ddg.getFileList().get(k);
            long mm = f.lastModified();
            String tmp = ddg.getFileList().get(k).getAbsolutePath();
            tmp = tmp.replace('\\', '/');
            String[] pathPropers = tmp.split(xxparser.getArchivePath());
            System.out.println(tmp + "      pathPropers.length   " + pathPropers.length);
            String[] pathParts = pathPropers[1].split("/");
            //System.out.println("           +pathParts.length " + pathParts.length);
            String YYYY = pathParts[1];
            String DDDD = pathParts[2];
            String fileName = pathParts[pathParts.length - 1];
            System.out.println("YYYY:  " + YYYY + "     DDDD:  " + DDDD);
            if (fsa.fileRead(tmp)) {
                String c = fsa.getFileContent().trim();

//                System.out.println(pns.utils.strings.RStrings.strToHash(c));
                MessageDigest digest;

                int y = gettingIntFromSTR(YYYY);
                int m = gettingIntFromSTR(DDDD.split("-")[0]);
                int d = gettingIntFromSTR(DDDD.split("-")[1]);
                System.out.println((new Date()) + " ;   file Modified     " + new Date(mm));
                FileMeasured fm = new FileMeasured(y, m, d, c, fileName, mm);

                fml.add(fm);
                System.out.println(k + " c==null " + c.length());
                System.out.println(k + " y " + fm.getYear());
                System.out.println(k + " m " + fm.getMonth());
                System.out.println(k + " d " + fm.getDate());
            }

        }
        return fml;
    }

    private int gettingIntFromSTR(String s) throws NumberFormatException {
        return Integer.parseInt(s);
    }

    private void insertFilesToArchive(List<FileMeasured> fml) {
        int k = 0;
        for (FileMeasured fm : fml) {
            System.out.println(""
                    + "++++++++++++++++++++++++++++++++++++++++++" + System.lineSeparator()
                    + " file:   " + fm.getFileName() + System.lineSeparator()
                    + "  :   =========>>>   " + fm.getStrHash() + "        " + fm.getIntHash() + System.lineSeparator() + ""
                    + "   fm.getFileName() " + fm.getFileName() + System.lineSeparator()
                    + "    fm.getDate()" + fm.getDate() + System.lineSeparator()
                    + "  fm.getMonth()" + fm.getMonth() + System.lineSeparator()
                    + "  fm.getYear() " + fm.getYear()
                    + ""
                    + "");
            String tmpMonth = "" + fm.getMonth();
            String tmpDate = "" + fm.getDate();
            if (fm.getMonth() < 10) {
                tmpMonth = "0" + tmpMonth;
            }
            if (fm.getDate() < 10) {
                tmpDate = "0" + tmpDate;
            }
            StringBuilder sbf = new StringBuilder();
            sbf.append(xxparser.getArchivePath() + '/');
            sbf.append(fm.getYear() + "/");
            sbf.append(tmpMonth + "-" + tmpDate + "/");
            sbf.append(fm.getFileName());
            getType(fm);
            fm.setFileType(fileType);

            if (!emA.contains(fm)) {

                System.out.println(new Date() + "   --------------------- Creating Archiv / Step 3: Adding file to Archive ---------------");
                emA.getTransaction().begin();
                emA.persist(fm);
                emA.getTransaction().commit();
                k++;

            }

            System.out.println("                      *******");
            System.out.println("    Try To Remove  aFile:  " + sbf.toString());
            System.out.println("                         ******");
            File ff = new File(sbf.toString());
            if (ff.exists()) {
                if (ff.delete()) {
                    System.out.println("  Deleting work have done! ");
                }
            }
        }
        System.out.println("  Number of Archive Operations:   " + k);

    }

}
