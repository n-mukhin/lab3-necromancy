package project.db;

public class DAOFactory {
    private static ResultDAO resultDAO;
    private static UserDAO userDAO;

    private static DAOFactory instance;

    public static DAOFactory getInstance() {
        if (instance == null)
            instance = new DAOFactory();
        return instance;
    }

    public ResultDAO getResultDAO() {
        if (resultDAO == null)
            resultDAO = new ResultDAOImpl();
        return resultDAO;
    }

    public UserDAO getUserDAO() {
        if (userDAO == null)
            userDAO = new UserDAOImpl();
        return userDAO;
    }
}
