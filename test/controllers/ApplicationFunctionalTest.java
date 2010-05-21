package controllers;

import org.junit.Test;

import play.mvc.Http.Response;
import play.test.FunctionalTest;

public class ApplicationFunctionalTest extends FunctionalTest {

    @Test
    public void testThatIndexPageWorks() {
        Response response = GET("/");
        assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset("utf-8", response);
    }
    
    
    @Test 
    public void testModuleRegular() {
    	Response response = GET("/application/module?name=regular");
    	assertIsOk(response);
        assertContentType("text/html", response);
        assertCharset("utf-8", response);
    } 
}