package usm.edu.cos420.Servlets;
import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import usm.edu.cos420.domain.BlogPost;

@SuppressWarnings("serial")
@WebServlet(name = "createBlogPost", value="/create")
public class createBlogPost extends HttpServlet {

	final String cloudDBUrl = "jdbc:postgresql:///%s?cloudSqlInstance=%s&amp;socketFactory=com.google.cloud.sql.postgres.SocketFactory&amp;user=%s&amp;password=%s";
	final String createDbQuery =  "CREATE TABLE IF NOT EXISTS posts ( id SERIAL PRIMARY KEY, "
			+ "title VARCHAR(255), author VARCHAR(255), description VARCHAR(255))";
	final String insertPost = "INSERT INTO posts "
			+ "(title, author, description) "
			+ "VALUES (?, ?, ?)";
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

		BlogPost post = new BlogPost();
		post.setTitle(req.getParameter("title"));
		post.setAuthor(req.getParameter("author"));
		post.setDescription(req.getParameter("description"));

		//Get DB information
		Properties properties = new Properties();
		properties.load(getClass().getClassLoader().getResourceAsStream("database.properties"));

		String dbUrl = String.format(cloudDBUrl,
				properties.getProperty("sql.dbName"), properties.getProperty("sql.instanceName"),
				properties.getProperty("sql.userName"), properties.getProperty("sql.password"));
		
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    PrintWriter out = resp.getWriter();

	    out.println("DBUrl "+dbUrl);
        try(Connection conn = DriverManager.getConnection(dbUrl)){
			
			out.println("Successfully got connection");
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(createDbQuery);

            stmt = conn.createStatement();
            final PreparedStatement createPostStmt = conn.prepareStatement(insertPost,Statement.RETURN_GENERATED_KEYS); 
		    createPostStmt.setString(1, post.getTitle());
		    createPostStmt.setString(2, post.getAuthor());
		    createPostStmt.setString(3, post.getDescription());

		    out.println("Before");
//		    String insertStmt = "INSERT INTO posts(title, author, description)\n"
//		    		+ "VALUES ('A', 'B','C')";
//		    stmt.executeUpdate(insertStmt);
		    
		    createPostStmt.executeUpdate();
		    out.println("After execute update");
            if(conn != null)
				conn.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

    out.println(
        "Article with the title: " + req.getParameter("title") + " by "
            + req.getParameter("author") + " and the content: "
            + req.getParameter("description") + " added." + dbUrl);
  }
}