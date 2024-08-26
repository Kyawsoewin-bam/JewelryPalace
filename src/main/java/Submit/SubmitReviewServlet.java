package Submit;

// import Submit.Review;  // Add this line at the top of your servlet
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class SubmitReviewServlet
 */
@WebServlet("/SubmitReviewServlet")
public class SubmitReviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SubmitReviewServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
	    PrintWriter out = response.getWriter();

	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        Class.forName("com.mysql.cj.jdbc.Driver");
	        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jewelrypalace", "root", "root");

	        String sql = "SELECT * FROM review";
	        pstmt = conn.prepareStatement(sql);
	        rs = pstmt.executeQuery();

	        List<Review> reviews = new ArrayList<>();

	        while (rs.next()) {
	            Review review = new Review();
	            review.setId(rs.getInt("id"));
	            review.setUserName(rs.getString("user_name"));
	            review.setContent(rs.getString("content"));
	            review.setCreatedAt(rs.getTimestamp("created_at")); // Fetch created_at
	            reviews.add(review);
	        }

	        request.setAttribute("reviews", reviews);
	        request.getRequestDispatcher("index.jsp").forward(request, response);

	    } catch (Exception e) {
	        e.printStackTrace();
	        out.println("An error occurred: " + e.getMessage());
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String action = request.getParameter("action");
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/jewelrypalace", "root", "root");

            if ("delete".equals(action)) {
                int reviewId = Integer.parseInt(request.getParameter("reviewId"));

                String deleteSql = "DELETE FROM review WHERE id = ?";
                pstmt = conn.prepareStatement(deleteSql);
                pstmt.setInt(1, reviewId);
                int result = pstmt.executeUpdate();

                if (result > 0) {
                    response.sendRedirect("SubmitReviewServlet");
                } else {
                    out.println("<script>alert('Failed to delete review. Please try again.'); location='index.jsp';</script>");
                }
            } else {
                String reviewContent = request.getParameter("reviewContent");
                String username = session.getAttribute("username").toString();

                // Log the review content and username
                System.out.println("Review Content: " + reviewContent);
                System.out.println("Username: " + username);

                String insertSql = "INSERT INTO review (user_name, content) VALUES (?, ?)";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, username);
                pstmt.setString(2, reviewContent);
                int result = pstmt.executeUpdate();

                // Log the result
                System.out.println("Insert Result: " + result);

                if (result > 0) {
                    response.sendRedirect("SubmitReviewServlet");
                } else {
                    out.println("<script>alert('Failed to submit review. Please try again.'); location='index.jsp';</script>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("An error occurred: " + e.getMessage());
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
