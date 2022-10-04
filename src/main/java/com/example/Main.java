/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@Controller
@SpringBootApplication
public class Main {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @PostConstruct
  public void start() throws IOException, InterruptedException {
    String last_article=null;

    while(true) {


      // File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      // File file = new File(jarFile.getParentFile().getParent(), "data.txt");


      File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
      String inputFilePath = jarFile.getParent() + File.separator + "data.txt";
      File file = new File(inputFilePath);


      // InputStream is = getClass().getClassLoader().getResourceAsStream("data.txt");
      // BufferedReader br = new BufferedReader(new InputStreamReader(is));
      InputStream is = new FileInputStream(file);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line = br.readLine();
      if(line != null){
        System.out.println("LAST ARTICLE:" + line);
        last_article = line;
      }
      is.close();




      Document doc = Jsoup.connect("https://www.iiscostanzodecollatura.edu.it/").get();
      String title = doc.title();
      Elements es = doc.getElementsByAttributeValue("class", "jsn-article");
      boolean first = true;
      int count =0;
      for (Element e : es) {
        count ++;
        // articolo
        Element article = e.children().first().children().first();
        String link_art = article.attr("href");
        String title_art = article.text();
        System.out.println(link_art + " - " + title_art);

        if(title_art.equalsIgnoreCase(last_article)){
          System.out.println("Trovato articolo già gestito");
          break;
        }

        if(last_article == null && first)
          last_article = title_art;

        if(first) {
          last_article = title_art;



          PrintWriter pw = new PrintWriter(file);
          // PrintWriter pw = new PrintWriter(new File(getClass().getClassLoader().getResource("data.txt").getFile()));
          pw.write(title_art);
          pw.close();
        }

        String urlString = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
        String apiToken = "5635342275:AAH06bwwFho8DXjCD";
        String apiToken_old = "-6cdfHQ-XYUbYYYBWg";
        String chatId = "@costanzo_news";
        String text = "MSG " + title_art + " " + "https://www.iiscostanzodecollatura.edu.it/" +link_art;
        urlString = String.format(urlString, apiToken+apiToken_old, chatId, text);
        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();
        conn.getInputStream();

        first = false;
        if(count > 4)
          break;
      }




      Thread.sleep(300000);
    }

  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/test")
  String test(Map<String, Object> model) {

    System.out.println("CIAOOOOOOOOOOOOOOOOOO");
    return "";
  }
  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
