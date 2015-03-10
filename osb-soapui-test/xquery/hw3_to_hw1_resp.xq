(:: pragma bea:global-element-parameter parameter="$sayHelloResponse1" element="ns1:sayHelloResponse" location="../business/wsdls/HelloWorld3.wsdl" ::)
(:: pragma bea:global-element-return element="ns0:sayHelloResponse" location="../proxy/wsdls/HelloWorld1.wsdl" ::)

declare namespace ns1 = "http://helloworld3.webservice.moodykettle.com";
declare namespace ns0 = "http://helloworld1.webservice.moodykettle.com";
declare namespace xf = "http://tempuri.org/osb-soapui-test/xquery/hw3_to_hw1_resp/";

declare function xf:hw3_to_hw1_resp($sayHelloResponse1 as element(ns1:sayHelloResponse))
    as element(ns0:sayHelloResponse) {
        <ns0:sayHelloResponse>
            <ns0:sayHelloReturn>{ data($sayHelloResponse1/ns1:sayHelloReturn) }</ns0:sayHelloReturn>
        </ns0:sayHelloResponse>
};

declare variable $sayHelloResponse1 as element(ns1:sayHelloResponse) external;

xf:hw3_to_hw1_resp($sayHelloResponse1)
