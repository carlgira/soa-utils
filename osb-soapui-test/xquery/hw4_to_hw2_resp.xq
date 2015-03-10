(:: pragma bea:global-element-parameter parameter="$sayHelloResponse1" element="ns0:sayHelloResponse" location="../business/wsdls/HelloWorld4.wsdl" ::)
(:: pragma bea:global-element-return element="ns1:sayHelloResponse" location="../proxy/wsdls/HelloWorld2.wsdl" ::)

declare namespace ns1 = "http://helloworld2.webservice.moodykettle.com";
declare namespace ns0 = "http://helloworld4.webservice.moodykettle.com";
declare namespace xf = "http://tempuri.org/osb-soapui-test/xquery/hw4_to_hw2_resp/";

declare function xf:hw4_to_hw2_resp($sayHelloResponse1 as element(ns0:sayHelloResponse))
    as element(ns1:sayHelloResponse) {
        <ns1:sayHelloResponse>
            <ns1:sayHelloReturn>{ data($sayHelloResponse1/ns0:sayHelloReturn) }</ns1:sayHelloReturn>
        </ns1:sayHelloResponse>
};

declare variable $sayHelloResponse1 as element(ns0:sayHelloResponse) external;

xf:hw4_to_hw2_resp($sayHelloResponse1)
