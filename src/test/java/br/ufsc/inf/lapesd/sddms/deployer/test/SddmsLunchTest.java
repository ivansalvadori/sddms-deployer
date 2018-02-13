package br.ufsc.inf.lapesd.sddms.deployer.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class SddmsLunchTest {

    @Test
    public void testLunch() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("java -jar sddms.jar");

        String line;
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        while ((line = bri.readLine()) != null) {
            System.out.println(line);
        }
        bri.close();
        while ((line = bre.readLine()) != null) {
            System.out.println(line);
        }
        bre.close();

    }
}