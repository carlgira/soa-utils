package com.carlgira.aq;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import oracle.AQ.*;
import oracle.jdbc.OracleConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
public class AQTest {


    @Autowired
    private DataSource dataSource;

    @RequestMapping("/enqueue")
    public String enqueue(@RequestParam(value = "message", required = true) String message) throws ClassNotFoundException, SQLException, AQException {

        Connection db_conn = dataSource.getConnection();
        OracleConnection oracleConnection = db_conn.unwrap(OracleConnection.class);

        AQSession session = createSession(oracleConnection);
        enqueueMsg(session, message);

        return "OK";
    }

    @RequestMapping("/enqueue_direct")
    public String enqueue_direct(@RequestParam(value = "message", required = true) String message) throws ClassNotFoundException, SQLException, AQException {

        Connection db_conn = DriverManager.getConnection( "jdbc:oracle:thin:@bd1_des:1522:OMDES","aq", "aq");
        db_conn.setAutoCommit(false);

        AQSession session = createSession(db_conn);
        enqueueMsg(session, message);
        db_conn.commit();

        return "OK";
    }

    public AQSession createSession(Connection db_conn)
    {
        AQSession aq_sess = null;
        try
        {
            Class.forName("oracle.AQ.AQOracleDriver");
            aq_sess = AQDriverManager.createAQSession(db_conn);
            System.out.println("Successfully created AQSession ");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return aq_sess;
    }

    public void enqueueMsg(AQSession aq_sess, String text)
            throws AQException, SQLException, ClassNotFoundException
    {
        Connection           db_conn   = null;
        AQQueue              queue     = null;
        AQMessage            message   = null;
        AQObjectPayload      payload   = null;
        AQEnqueueOption      eq_option = null;

        db_conn = ((AQOracleSession)aq_sess).getDBConnection();

        queue = aq_sess.getQueue("aq", "MSG_QUEUE");

        message = queue.createMessage();

        CMSG cmsg = new CMSG("AQ.CMSG", "subject",text );

        payload = message.getObjectPayload();
        payload.setPayloadData(cmsg);
        eq_option = new AQEnqueueOption();

        queue.enqueue(eq_option, message);
    }

}
