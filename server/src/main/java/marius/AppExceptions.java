package marius;

/** This class is used for certralize all the class exception of the services
 */
public class AppExceptions {
    public static class UserNotAuthException extends RuntimeException{
        public UserNotAuthException(String username){
            super("User with username="+username+" is not authorized to registrate");
        }

    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String s) {
           super("User with username="+s+" not found");
        }
    }

    public static class UserSamePasswordExcemptio extends RuntimeException {
        public UserSamePasswordExcemptio(String s) {
            super(s);
        }
    }
}
