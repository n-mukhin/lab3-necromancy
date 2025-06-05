package project;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import lombok.Data;
import project.db.DAOFactory;
import project.db.UserDAO;
import project.entity.WebUser;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Data
@Named
@ViewScoped
public class AuthBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String registrationUsername;
    private String registrationPassword;
    private boolean isLoginMode = true;

    private final UserDAO userDAO = DAOFactory.getInstance().getUserDAO();


    public String login() {
        WebUser user = userDAO.getUserByUsername(username);
        if (user != null) {
            String hashedPassword = hashPassword(password, Base64.getDecoder().decode(user.getSalt()));
            if (hashedPassword.equals(user.getPasswordHash())) {

                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", user);
                return "main.xhtml?faces-redirect=true";
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid login", null));
        return null;
    }

    public String register() {
        if (registrationUsername == null || registrationUsername.trim().isEmpty() ||
                registrationPassword == null || registrationPassword.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid registration", null));
            return null;
        }

        if (registrationUsername.length() < 3) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username must be at least 3 characters long", null));
            return null;
        }

        try {
            if (userDAO.getUserByUsername(registrationUsername) != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username already exists", null));
                return null;
            }

            byte[] saltBytes = generateSalt();
            String salt = Base64.getEncoder().encodeToString(saltBytes);

            String hashedPassword = hashPassword(registrationPassword, saltBytes);

            WebUser newUser = WebUser.builder()
                    .username(registrationUsername)
                    .passwordHash(hashedPassword)
                    .salt(salt)
                    .build();

            userDAO.addUser(newUser);

            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", newUser);
            return "main.xhtml?faces-redirect=true";
        } catch (PersistenceException e) {
            if (e.getCause() instanceof ConstraintViolationException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username already exists", null));
                return null;
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registration failed", null));
            return null;
        }
    }

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean isLoggedIn() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user") instanceof WebUser;
    }

    public void switchToLoginMode() {
        isLoginMode = true;
    }

    public void switchToRegistrationMode() {
        isLoginMode = false;
    }

    public boolean isLoginMode() {
        return isLoginMode;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "auth.xhtml?faces-redirect=true";
    }
}
