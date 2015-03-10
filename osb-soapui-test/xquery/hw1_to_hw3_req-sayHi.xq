(:: pragma bea:global-element-parameter parameter="$sayHi1" element="ns0:sayHi" location="../proxy/wsdls/HelloWorld1.wsdl" ::)
(:: pragma bea:global-element-return element="ns1:sayHello" location="../business/wsdls/HelloWorld3.wsdl" ::)

declare namespace ns1 = "http://helloworld3.webservice.moodykettle.com";
declare namespace ns0 = "http://helloworld1.webservice.moodykettle.com";
declare namespace xf = "http://tempuri.org/osb-soapui-test/xquery/hw1_to_hw3_req-sayHi/";

declare function xf:hw1_to_hw3_req-sayHi($sayHi1 as element(ns0:sayHi))
    as element(ns1:sayHello) {
        <ns1:sayHello>
            <ns1:name>{ data($sayHi1/ns0:name) }</ns1:name>
        </ns1:sayHello>
};

declare variable $sayHi1 as element(ns0:sayHi) external;

xf:hw1_to_hw3_req-sayHi($sayHi1)
