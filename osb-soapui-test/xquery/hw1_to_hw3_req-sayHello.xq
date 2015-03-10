(:: pragma bea:global-element-parameter parameter="$sayHello1" element="ns0:sayHello" location="../proxy/wsdls/HelloWorld1.wsdl" ::)
(:: pragma bea:global-element-return element="ns1:sayHello" location="../business/wsdls/HelloWorld3.wsdl" ::)

declare namespace ns1 = "http://helloworld3.webservice.moodykettle.com";
declare namespace ns0 = "http://helloworld1.webservice.moodykettle.com";
declare namespace xf = "http://tempuri.org/osb-soapui-test/xquery/hw1_to_hw3_req/";

declare function xf:hw1_to_hw3_req($sayHello1 as element(ns0:sayHello))
    as element(ns1:sayHello) {
        <ns1:sayHello>
            <ns1:name>{ data($sayHello1/ns0:name) }</ns1:name>
        </ns1:sayHello>
};

declare variable $sayHello1 as element(ns0:sayHello) external;

xf:hw1_to_hw3_req($sayHello1)
