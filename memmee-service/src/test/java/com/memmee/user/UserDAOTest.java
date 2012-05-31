package com.memmee.user;

import com.memmee.user.dao.UserDAO;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.db.Database;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.DatabaseFactory;
import org.skife.jdbi.v2.Handle;

import java.sql.Types;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class UserDAOTest {
    private final DatabaseConfiguration mysqlConfig = new DatabaseConfiguration();
    {
        LoggingFactory.bootstrap();
        mysqlConfig.setUrl("jdbc:mysql://localhost:8889/commons");
        mysqlConfig.setUser("commons");
        mysqlConfig.setPassword("commons");
        mysqlConfig.setDriverClass("com.mysql.jdbc.Driver");
        mysqlConfig.setValidationQuery("SELECT 1 FROM commons.user");
    }
    private final Environment environment = mock(Environment.class);
    private final DatabaseFactory factory = new DatabaseFactory(environment);
    private Database database;

    @Before
    public void setUp() throws Exception {
        this.database = factory.build(mysqlConfig, "mysql");
        final Handle handle = database.open();
        try {
            handle.createCall("DROP TABLE IF EXISTS user").invoke();
//            handle.createCall(
//                    "CREATE TABLE user ( id int(11) NOT NULL AUTO_INCREMENT, firstName varchar(1024) DEFAULT NULL, lastName varchar(1024) DEFAULT NULL, email varchar(4096) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB AUTO_INCREMENT=10")
//                    .invoke();

            handle.createCall(
                    "CREATE TABLE `user` (\n" +
                            "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                            "  `firstName` varchar(1024) DEFAULT NULL,\n" +
                            "  `lastName` varchar(1024) DEFAULT NULL,\n" +
                            "  `email` varchar(4096) NOT NULL,\n" +
                            "  `apiKey` varchar(1024) DEFAULT NULL,\n" +
                            "  `apiDate` date DEFAULT NULL,\n" +
                            "  `creationDate` date NOT NULL,\n" +
                            "  PRIMARY KEY (`id`)\n" +
                            ") ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1"
            ).invoke();

//            handle.createStatement("INSERT INTO people VALUES (?, ?, ?)")
//                    .bind(0, "Coda Hale")
//                    .bind(1, "chale@yammer-inc.com")
//                    .bind(2, 30)
//                    .execute();
//            handle.createStatement("INSERT INTO people VALUES (?, ?, ?)")
//                    .bind(0, "Kris Gale")
//                    .bind(1, "kgale@yammer-inc.com")
//                    .bind(2, 32)
//                    .execute();
//            handle.createStatement("INSERT INTO people VALUES (?, ?, ?)")
//                    .bind(0, "Old Guy")
//                    .bindNull(1, Types.VARCHAR)
//                    .bind(2, 99)
//                    .execute();
        } catch (Exception e)
        {
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

//    @Test
//    public void createsAValidDBI() throws Exception {
//        final Handle handle = database.open();
//        try {
//            final Query<String> names = handle.createQuery("SELECT name FROM people WHERE age < ?")
//                    .bind(0, 50)
//                    .map(StringMapper.FIRST);
//            assertThat(ImmutableList.copyOf(names),
//                    is(ImmutableList.of("Coda Hale", "Kris Gale")));
//        } finally {
//            handle.close();
//        }
//    }

    @Test
    public void testSave() throws Exception {
        final Handle handle = database.open();
        final UserDAO dao = database.open(UserDAO.class);
        dao.insert(new Long(1), "Adam", "Parrish", "aparrish@neosavvy.com", "apiKey", new Date(), new Date());
    }

    @Test
    public void managesTheDatabaseWithTheEnvironment() throws Exception {
        final Database db = factory.build(mysqlConfig, "hsql");

        verify(environment).manage(db);
    }

//    @Test
//    public void sqlObjectsCanAcceptOptionalParams() throws Exception {
//        final PersonDAO dao = database.open(PersonDAO.class);
//        try {
//            assertThat(dao.findByName(Optional.of("Coda Hale")),
//                    is("Coda Hale"));
//        } finally {
//            database.close(dao);
//        }
//    }

//    @Test
//    public void sqlObjectsCanReturnImmutableLists() throws Exception {
//        final PersonDAO dao = database.open(PersonDAO.class);
//        try {
//            assertThat(dao.findAllNames(),
//                    is(ImmutableList.of("Coda Hale", "Kris Gale", "Old Guy")));
//        } finally {
//            database.close(dao);
//        }
//    }

//    @Test
//    @SuppressWarnings("CallToPrintStackTrace")
//    public void pingWorks() throws Exception {
//        try {
//            database.ping();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            fail("shouldn't have thrown an exception but did");
//        }
//    }
}

