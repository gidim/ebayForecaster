package ebayForcaster;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.io.File;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.experiment.InstanceQuery;

public class DBConnect {

    String DRIVER = "com.mysql.jdbc.Driver";
    String LOCATION = "107.170.18.96:3306";
    String DATABASE = "ebayForcaster";
    String USER = "ebay";
    String PASSWORD = "ebay3344";

    private Connection con;
    private Statement st;
    private ResultSet rs;

    public DBConnect() {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + LOCATION + "/"
                    + DATABASE, USER, PASSWORD);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            st = con.createStatement();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void insertEbayItems(ArrayList<EbayItem> itemList)
            throws SQLException {

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        for (EbayItem item : itemList) {

            PreparedStatement preparedStatement = null;
            String query = "INSERT INTO Item (itemId, title,"
                    + "categoryId, productId, postalCode, country, feedbackScore,"
                    + "positiveFeedbackPercent, feedbackRatingStar, topRatedSeller,"
                    + "shippingServiceCost, shippingType, expeditedShipping,"
                    + "oneDayShippingAvailable, handlingTime, convertedCurrentPrice,"
                    + "bidCount, bestOfferEnabled, buyItNowAvailable, endTime,"
                    + "listingType, returnsAccepted, conditionId, topRatedListing) "
                    + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,"
                    + "?,?,?,?,?,?) ON DUPLICATE KEY UPDATE itemId=VALUES(itemId);";

            try {
                preparedStatement = con.prepareStatement(query);
                
                preparedStatement.setString(1, item.getEbayItemID());
                preparedStatement.setString(2, item.getName());
                preparedStatement.setString(3, item.getCatID());
                preparedStatement.setString(4, item.getProductID());
                preparedStatement.setString(5, item.getZipcode());
                preparedStatement.setString(6, item.getCountry());
                preparedStatement.setInt(7, item.getFeedbackCount());
                preparedStatement.setDouble(8, item.getFeedbackPercentPositive());
                preparedStatement.setString(9, item.getFeedbackRatingStar());
                preparedStatement.setBoolean(10, item.isTopRatedSeller());
                preparedStatement.setDouble(11, item.getShippingPrice());
                preparedStatement.setString(12, item.getShippingType());
                preparedStatement.setBoolean(13, item.isExpediatedShipping());
                preparedStatement.setBoolean(14,item.isOneDayShippingAvailable());
                preparedStatement.setInt(15, item.getHandlingTime());
                preparedStatement.setDouble(16, item.getPrice());
                preparedStatement.setInt(17, item.getBids());
                preparedStatement.setBoolean(18, item.isBestOffer());
                preparedStatement.setBoolean(19, item.isBuyItNow());
                preparedStatement.setString(20, sdf.format(item.getEndTime()));
                preparedStatement.setString(21, item.getListingType());
                preparedStatement.setBoolean(22, item.isReturnsAccepted());
                preparedStatement.setInt(23, item.getConditionID());
                preparedStatement.setBoolean(24, item.isTopRatedListing());

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
            	e.printStackTrace();
            } finally {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            }
        }
    }

    public ArrayList<EbayItem> getEbayItems(String productId)
            throws SQLException, ParseException {
        ArrayList<EbayItem> items = new ArrayList<EbayItem>();

        String query = "SELECT * " + "FROM Items " + "WHERE sproductId = "
                + productId;
        rs = st.executeQuery(query);

        while (rs.next()) {
            String ebayItemID = rs.getString("itemId");
            String name = rs.getString("title");
            String catID = rs.getString("categoryId");
            String productID = rs.getString("productId");
            String zipcode = rs.getString("postalCode");
            String country = rs.getString("country");
            int feedbackCount = rs.getInt("feedbackScore");
            double feedbackPercentPositive = rs.getDouble("positiveFeedbackPercent");
            String feedbackRatingStar = rs.getString("feedbackRatingStar");
            boolean topRatedSeller = rs.getBoolean("topRatedSeller");
            double shipping = rs.getDouble("shippingServiceCost");
            String shippingType = rs.getString("shippingType");
            boolean expediatedShipping = rs.getBoolean("expediatedShipping");
            boolean oneDayShippingAvailable = rs
                    .getBoolean("oneDayShippingAvailable");
            int handlingTime = rs.getInt("handlingTime");
            double price = rs.getDouble("convertedCurrentPrice");
            int bids = rs.getInt("bidCount");
            boolean bestOffer = rs.getBoolean("bestOfferEnabled");
            boolean buyItNow = rs.getBoolean("buyItNowAvailable");
            Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.ENGLISH).parse(rs.getString("endTime"));
            String listingType = rs.getString("listingType");
            boolean returnsAccepted = rs.getBoolean("returnsAccepted");
            int conditionID = rs.getInt("conditionId");
            boolean topRatedListing = rs.getBoolean("topRatedListing");
            EbayItem temp = new EbayItem(ebayItemID, name, catID, productID,
                    zipcode, country, feedbackCount, feedbackPercentPositive,
                    feedbackRatingStar, topRatedSeller, shippingType, shipping,
                    expediatedShipping, oneDayShippingAvailable, handlingTime,
                    price, bids, bestOffer, buyItNow, endTime, listingType,
                    returnsAccepted, conditionID, topRatedListing);
            items.add(temp);
        }

        return items;
    }

    public Instances getWekaInstance(int productId, String[] attributes) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        for(int i = 0; i < attributes.length; i++){
            sb.append(attributes[i]);
            if(i+1 < attributes.length){
                sb.append(", ");
            }
        }
        sb.append(" FROM Item WHERE productId = \"");
        sb.append(Integer.toString(productId));
        sb.append("\"");

        InstanceQuery query = new InstanceQuery();
        query.setUsername("ebay");
        query.setPassword("ebay3344");
        query.setQuery(sb.toString());

        return query.retrieveInstances();
    }

    public void getData() throws SQLException {
            String query = "SELECT * FROM Item";
            rs = st.executeQuery(query);

            while (rs.next()) {
                String name = rs.getString("itemId");
            }

    }

    @Override
    protected void finalize() throws Throwable {
        try {
            st.close(); // close open statement
            con.close(); // close open connection
        } finally {
            super.finalize();
        }
    }

}