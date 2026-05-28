package marius;

/** This class is used for certralize all the class exception of the services
 */
public class AppExceptions {
    public static class UserNotAuthException extends RuntimeException{
        public UserNotAuthException(String username){
            super("User with username="+username+" is not authorized to registrate");
        }

    }
}
