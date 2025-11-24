import com.sap.gateway.ip.core.customdev.util.Message
 
def Message processData(Message message) {
 
    def props = message.getProperties()
    def ex = props.get("CamelExceptionCaught")
    def errorText = props.get("errorText")
 
    def errorMsg = ""
 
    if (ex != null) {
        // Capture the actual exception message and class
        errorMsg = "Exception: ${ex.getClass().getSimpleName()} - ${ex.getMessage()}"
    } else if (errorText) {
        // Capture predefined error text if available
        errorMsg = "Error Text: ${errorText}"
    } else {
        // Generic fallback
        errorMsg = "Unknown error occurred during processing."
    }
 
    // Optionally log the message to the body for monitoring visibility
    message.setBody("Error occurred during processing: ${errorMsg}")
 
    // Throw an exception to make the IFlow fail visibly (red in monitor)
    throw new Exception(errorMsg)
}
