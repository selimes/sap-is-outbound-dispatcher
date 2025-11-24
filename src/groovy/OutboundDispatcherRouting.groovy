import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.ITApiFactory
import com.sap.it.api.mapping.ValueMappingApi
import groovy.json.JsonSlurper
 
Message processData(Message message) {
 
    def Body = message.getBody(java.lang.String) as String
    
    // --- Find Receiver, Sender, MessageID
    def xmlObject
        try {
            def xmlParser = new XmlSlurper()
            xmlObject = xmlParser.parseText(Body)
        } catch (Throwable t) {
        throw new IllegalArgumentException("Payload is not valid: ${t.message}", t)
        }
 
    def Receiver = xmlObject.'**'.find { it.name() == 'Receiver' }?.text() ?: "Not Found"
    def MessageID = xmlObject.'**'.find { it.name() == 'messageId' }?.text() ?: "Not Found"
    def Sender = xmlObject.'**'.find { it.name() == 'Sender' }?.text() ?: "Not Found" 
    
    // --- Read Value Mapping
    def vmApi = ITApiFactory.getApi(ValueMappingApi.class, null)
    def mapped = vmApi.getMappedValue("Source", "Identifier", Receiver, "Target", "Values")
    if (!mapped) {
        throw new NoSuchElementException("No value mapping found for Route: ${Receiver}")
    }
 
    // --- Parse JSON
    def jsonObject 
        try {
            def jsonParser = new JsonSlurper()
            jsonObject = jsonParser.parseText(mapped)
        } catch (Throwable t) {
        throw new IllegalArgumentException("Mapped value is not valid JSON: ${t.message}", t)
        }
 
    def Endpoint                = jsonObject?.Endpoint 
    def Certificate             = jsonObject?.Certificate 
    def MessageType             = jsonObject?.MessageType 
    def SOAPAction              = jsonObject?.SOAPAction 
    def XServerAuthentication   = jsonObject?.XServerAuthentication
 
    if (SOAPAction) {
        message.setHeader("SOAPAction", SOAPAction )
    }
    if (XServerAuthentication) {
        message.setHeader("X-Server-Authentication", XServerAuthentication )
    }    
    if (!Endpoint) {
         throw new IllegalArgumentException('JSON value mapping is missing "Endpoint".')
    }
    if (!Certificate) {
        throw new IllegalArgumentException('JSON value mapping is missing "Certificate".')
    }
    // --- Set standard searchable headers (required for KPI) ---
    message.setHeader("SAP_ApplicationID", MessageID)
    message.setHeader("SAP_Sender",        Sender)
    message.setHeader("SAP_Receiver",      Receiver)
    message.setHeader("SAP_MessageType",   MessageType)
 
    // --- Set Properties
    message.setProperty('ReceiverAddress', Endpoint as String)
    message.setProperty('ReceiverCertificate', Certificate as String)
    return message
}
