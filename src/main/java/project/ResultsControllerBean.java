package project;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.db.DAOFactory;
import project.entity.ResultEntity;
import project.entity.WebUser;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

@Data
@NoArgsConstructor
@Named("resultsControllerBean")
@SessionScoped
public class ResultsControllerBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ResultsControllerBean.class);

    @Inject
    private XBean xBean;

    @Inject
    private YBean yBean;

    @Inject
    private RBean rBean;

    private ArrayList<ResultEntity> results = new ArrayList<>();

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Object userObj = facesContext.getExternalContext().getSessionMap().get("user");

        if (userObj instanceof WebUser) {
            WebUser loggedInUser = (WebUser) userObj;
            Collection<ResultEntity> userResults = DAOFactory.getInstance().getResultDAO().getResultsByUser(loggedInUser.getId());
            results = new ArrayList<>(userResults);
            Collections.reverse(results);

            int size = results.size();
            for (int i = 0; i < size; i++) {
                results.get(i).setReverseId(size - i);
            }

            log.info("User {} has {} results.", loggedInUser.getUsername(), results.size());
        } else {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must be logged in to view results!", null));
            log.error("Session attribute 'user' is not of type WebUser.");
            results = new ArrayList<>();
        }
    }

    public void addResult() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Object userObj = facesContext.getExternalContext().getSessionMap().get("user");

        if (!(userObj instanceof WebUser)) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must be logged in to submit results!", null));
            log.error("Attempt to add result without a valid logged-in user.");
            return;
        }

        WebUser loggedInUser = (WebUser) userObj;
        long startTime = System.nanoTime();

        try {
            xBean.validateXBeanValue(xBean.getX());
            yBean.validateYBeanValue(yBean.getY());
            rBean.validateRBeanValue(rBean.getR());
        } catch (ValidatorException ve) {
            facesContext.addMessage(null, ve.getFacesMessage());
            log.error("Validation failed: {}", ve.getFacesMessage().getSummary());
            return;
        }

        Boolean hitResult = calculateHitResult(xBean.getX(), yBean.getY(), rBean.getR());
        long endTime = System.nanoTime();
        BigDecimal executionTime = calculateExecutionTime(startTime, endTime);
        log.debug("Calculated executionTime: {} ms", executionTime);

        // Manual construction instead of Lombok builder:
        ResultEntity entity = new ResultEntity();
        entity.setX(xBean.getX());
        entity.setY(yBean.getY());
        entity.setR(rBean.getR());
        entity.setHitResult(hitResult);
        entity.setHitTime(LocalTime.now());
        entity.setExecutionTime(executionTime);
        entity.setUsers(new HashSet<>()); // initialize the Set

        // Add the user to the relationship
        entity.getUsers().add(loggedInUser);
        results.add(0, entity);
        entity.setReverseId(results.size());

        try {
            DAOFactory.getInstance().getResultDAO().addNewResult(entity);
            log.info("Added new result to the db: X={}, Y={}, R={}, User={}, Execution Time={} ms",
                    xBean.getX(), yBean.getY(), rBean.getR(), loggedInUser.getUsername(), executionTime);
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to add the result.", null));
            log.error("Error adding result to the db: {}", e.getMessage());
            return;
        }

        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Result added successfully! Execution Time: " + executionTime + " ms", null));
        resetInputs();

        for (int i = 0; i < results.size(); i++) {
            results.get(i).setReverseId(results.size() - i);
        }
    }

    public void removeAllResults() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Object userObj = facesContext.getExternalContext().getSessionMap().get("user");

        if (!(userObj instanceof WebUser)) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must be logged in to remove results!", null));
            log.error("Attempt to remove results without a valid logged-in user.");
            return;
        }

        WebUser loggedInUser = (WebUser) userObj;

        try {
            Collection<ResultEntity> userResults = DAOFactory.getInstance().getResultDAO().getResultsByUser(loggedInUser.getId());

            for (ResultEntity result : userResults) {
                DAOFactory.getInstance().getResultDAO().deleteResult(result);
                results.remove(result);
            }

            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "All results removed successfully!", null));
            log.info("User {} removed all their results.", loggedInUser.getUsername());
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to remove all results.", null));
            log.error("Error removing all results for user {}: {}", loggedInUser.getUsername(), e.getMessage());
        }
    }

    public Boolean calculateHitResult(Double x, Double y, Double r) {
        if (isPointInLeftTriangle(x, y, r)) {
            return true;
        }

        if (x >= 0 && x <= r && y <= 0 && y >= -r / 2) {
            return true;
        }

        if (x <= 0 && y >= 0 && (x * x + y * y) <= (r / 2) * (r / 2)) {
            return true;
        }

        return false;
    }

    private Boolean isPointInLeftTriangle(Double x, Double y, Double r) {
        double xA = 0, yA = 0;
        double xB = -r, yB = 0;
        double xC = 0, yC = -r;

        double areaABC = 0.5 * Math.abs(xA * (yB - yC) + xB * (yC - yA) + xC * (yA - yB));
        double areaPAB = 0.5 * Math.abs(x * (yB - yA) + xB * (yA - y) + xA * (y - yB));
        double areaPBC = 0.5 * Math.abs(x * (yC - yB) + xB * (y - yC) + xC * (yB - y));
        double areaPCA = 0.5 * Math.abs(x * (yA - yC) + xC * (y - yA) + xA * (yC - y));

        return Math.abs(areaPAB + areaPBC + areaPCA - areaABC) < 1e-6;
    }


    public BigDecimal calculateExecutionTime(long startTime, long endTime) {
        long durationInNanos = endTime - startTime;
        BigDecimal timeInMs = BigDecimal.valueOf(durationInNanos).divide(BigDecimal.valueOf(1_000_000), 3, RoundingMode.HALF_UP);
        return timeInMs;
    }

    public LocalDateTime getResultTimestamp(ResultEntity result) {
        return result.getResultTimestamp();
    }

    private void resetInputs() {
        xBean.setX(0.0);
        yBean.setY(0.0);
        rBean.setR(1.0);
    }

    public int getResultCount() {
        return results.size();
    }

    public int getReverseIndex(int index) {
        return results.size() - index;
    }

}
