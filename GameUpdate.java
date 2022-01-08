
import java.io.Serializable;

// Simple class for containing the game update message to be sent over the server
// The class implements serializable so that it can be converted to an output byte stream
// After being received by the input stream, it can then be converted back into an instance of the object
public class GameUpdate implements Serializable {

    private String updateText;

    public GameUpdate(String updateText) {
        this.updateText = updateText;
    }

    public String toString() {
        return this.updateText;
    }
}
