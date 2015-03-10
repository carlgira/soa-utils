(:: pragma bea:global-element-parameter parameter="$sayHelloResponse1" element="ns1:sayHelloResponse" location="../business/wsdls/HelloWorld3.wsdl" ::)
(:: pragma bea:global-element-return element="ns0:sayHello" location="../business/wsdls/HelloWorld4.wsdl" ::)

declare namespace ns1 = "http://helloworld3.webservice.moodykettle.com";
declare namespace ns0 = "http://helloworld4.webservice.moodykettle.com";
declare namespace xf = "http://tempuri.org/osb-soapui-test/xquery/hw3_to_hw4_req/";

declare function xf:hw3_to_hw4_req($sayHelloResponse1 as element(ns1:sayHelloResponse))
    as element(ns0:sayHello) {
        <ns0:sayHello>
            <ns0:name>{ data($sayHelloResponse1/ns1:sayHelloReturn) }</ns0:name>
        </ns0:sayHello>
};

declare variable $sayHelloResponse1 as element(ns1:sayHelloResponse) external;

xf:hw3_to_hw4_req($sayHelloResponse1)
