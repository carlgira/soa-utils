package com.carlgira.aq;

import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;

/**
 * Created by cgiraldo on 16/10/2017.
 */
public class CMSG implements SQLData {

    private String subject;
    private String text;
    private String sql_type;

    public CMSG(){
    }

    public CMSG(String sql_type, String subject, String text){
        this.sql_type = sql_type;
        this.subject = subject;
        this.text = text;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getSQLTypeName() throws SQLException {
        return sql_type;
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        this.sql_type = typeName;
        this.subject = stream.readString();
        this.text = stream.readString();
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeString(this.subject);
        stream.writeString(this.text);
    }
}