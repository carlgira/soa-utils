-- AS SYS
DROP USER aq CASCADE;
CREATE USER aq IDENTIFIED BY aq;
GRANT CONNECT, RESOURCE, aq_administrator_role TO aq; 
GRANT EXECUTE ON dbms_aq TO aq;
GRANT EXECUTE ON dbms_aqadm TO aq;
ALTER USER aq quota 50m on system;

begin 
    dbms_aqadm.grant_system_privilege ('ENQUEUE_ANY','AQ',FALSE); 
    dbms_aqadm.grant_system_privilege ('DEQUEUE_ANY','AQ',FALSE); 
end; 
/ 

-- AS AQ
CREATE type CMSG as object (subject VARCHAR2(30), text VARCHAR2(80)); 

begin 
    DBMS_AQADM.CREATE_QUEUE_TABLE ( queue_table => 'aq.objmsgs80_qtab', queue_payload_type => 'aq.CMSG', multiple_consumers => TRUE );
    DBMS_AQADM.CREATE_QUEUE ( queue_name => 'MSG_QUEUE', queue_table => 'aq.objmsgs80_qtab'); 
    DBMS_AQADM.START_QUEUE ( queue_name => 'MSG_QUEUE'); 
 end; 
 /  
 
create procedure enqueue_msg2( p_msg in varchar2 ) as 
     enqueue_options dbms_aq.enqueue_options_t; 
     message_properties dbms_aq.message_properties_t; 
     message_handle RAW(16); 
     message aq.message_typ; 
BEGIN 
 message := message_typ('NORMAL MESSAGE', p_msg ); 
 dbms_aq.enqueue(queue_name => 'msg_queue', 
 enqueue_options => enqueue_options, 
 message_properties => message_properties, 
 payload => message, 
 msgid => message_handle); 
 end; 
 / 


create table message_table( msg varchar2(4000) );

create or replace procedure notifyCB( context raw, reginfo sys.aq$_reg_info, descr sys.aq$_descriptor, 	payload raw, payloadl number) as 
dequeue_options dbms_aq.dequeue_options_t; 
message_properties dbms_aq.message_properties_t; 
message_handle RAW(16); 
message aq.message_typ; 
BEGIN 
	dequeue_options.msgid := descr.msg_id; 
	dequeue_options.consumer_name := descr.consumer_name; 
	DBMS_AQ.DEQUEUE(queue_name => descr.queue_name,  dequeue_options => dequeue_options, message_properties => message_properties, payload => message, msgid => message_handle); 
	insert into message_table values  ( 'Dequeued and processed "' || message.text || '"' ); 
	COMMIT; 
END; 
/ 

begin 
	dbms_aqadm.add_subscriber ( queue_name => 'aq.msg_queue', subscriber => sys.aq$_agent( 'recipient', null, null ) ); 
end; 


BEGIN 
	dbms_aq.register ( sys.aq$_reg_info_list(  sys.aq$_reg_info('AQ.MSG_QUEUE:RECIPIENT', DBMS_AQ.NAMESPACE_AQ, 'plsql://AQ.notifyCB',  HEXTORAW('FF')) ) , 1 ); 
end; 
/

exec enqueue_msg2( 'This is a test....' ); 
commit; 
select * from message_table;


jpub -user=aq/aq -sql=aq.Message_typ -case=mixed -usertypes=oracle -methods=false  -compatible=CustomDatum 