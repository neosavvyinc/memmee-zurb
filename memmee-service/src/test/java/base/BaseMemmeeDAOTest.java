package base;

import org.junit.After;
import org.junit.Before;
import org.skife.jdbi.v2.Handle;

/**
 * Created with IntelliJ IDEA.
 * User: trevorewen
 * Date: 7/24/12
 * Time: 9:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class BaseMemmeeDAOTest extends AbstractMemmeeDAOTest {

    @Before
    public void setUp() throws Exception {
        this.database = factory.build(mysqlConfig, "mysql");
        final Handle handle = database.open();
        try {

            handle.createCall("DROP TABLE IF EXISTS user").invoke();
            handle.createCall("DROP TABLE IF EXISTS memmee").invoke();
            handle.createCall("DROP TABLE IF EXISTS attachment").invoke();
            handle.createCall("DROP TABLE IF EXISTS theme").invoke();
            handle.createCall("DROP TABLE IF EXISTS inspiration").invoke();

            handle.createCall(
                    "CREATE TABLE `user` (\n" +
                            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "  `firstName` varchar(1024) DEFAULT NULL,\n" +
                            "  `lastName` varchar(1024) DEFAULT NULL,\n" +
                            "  `email` varchar(4096) NOT NULL,\n" +
                            "  `password` varchar(4096) NOT NULL,\n" +
                            "  `apiKey` varchar(1024) DEFAULT NULL,\n" +
                            "  `apiDate` date DEFAULT NULL,\n" +
                            "  `creationDate` date NOT NULL,\n" +
                            "  PRIMARY KEY (`id`)\n" +
                            ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1"
            ).invoke();
            handle.createCall(
                    "CREATE TABLE `memmee` (\n" +
                            "`id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "`userId` int(11) NOT NULL,\n" +
                            "`attachmentId` int(11) DEFAULT NULL,\n" +
                            "`inspirationId` int(11) DEFAULT NULL,\n" +
                            "`lastUpdateDate` datetime NOT NULL,\n" +
                            "`creationDate` datetime NOT NULL,\n" +
                            "`displayDate` datetime NOT NULL,\n" +
                            "`text` varchar(4096) DEFAULT NULL,\n" +
                            "`shareKey` varchar(1024) DEFAULT NULL,\n" +
                            "`themeId` int(11) DEFAULT NULL,\n" +
                            "PRIMARY KEY (`id`)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1"
            ).invoke();
            handle.createCall(
                    "CREATE TABLE `attachment` (\n" +
                            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "  `memmeeId` int(11) DEFAULT NULL,\n" +
                            "  `filePath` varchar(1024) DEFAULT NULL,\n" +
                            "  `thumbFilePath` varchar(1024) DEFAULT NULL,\n" +
                            "  `type` varchar(20) DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`id`)\n" +
                            ") ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3"

            ).invoke();
            handle.createCall(
                    "CREATE TABLE `theme` (\n" +
                            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "  `name` varchar(100) DEFAULT NULL,\n" +
                            "  `stylePath` varchar(1024) DEFAULT NULL,\n" +
                            "  PRIMARY KEY (`id`)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1"
            ).invoke();
            handle.createCall(
                    "CREATE TABLE `inspiration` (\n" +
                            "`id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "`text` varchar(1000) NOT NULL,\n" +
                            "`creationDate` datetime NOT NULL,\n" +
                            "`lastUpdateDate` datetime NOT NULL,\n" +
                            "PRIMARY KEY (`id`)\n" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1"
            ).invoke();

        } catch (Exception e) {
            System.err.println(e);

        } finally {
            handle.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        database.stop();
        this.database = null;
    }
}