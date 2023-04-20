package sd2223.trab1.clients.soap;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.soap.FeedsService;

import jakarta.xml.ws.Service;
import jakarta.xml.ws.BindingProvider;

/**
 * Class SoapFeedsClient - A soap feeds client
 *
 * @author Francisco Parrinha	58369
 * @author Martin Magdalinchev	58172
 */
public class SoapFeedsClient extends SoapClient implements Feeds {

    public SoapFeedsClient(URI serverURI) {
        super(serverURI);

    }

    private FeedsService stub;

    synchronized private FeedsService stub() {
        if(stub == null) {
            QName QNAME = new QName(FeedsService.NAMESPACE, FeedsService.NAME);
            Service service = Service.create(super.toURL(super.uri + WSDL), QNAME);
            this.stub = service.getPort(FeedsService.class);
            super.setTimeouts((BindingProvider) stub);
        }
        return stub;
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message message) {

        return super.reTry( () -> super.toJavaResult( () -> stub().postMessage(user, pwd, message)));
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {

        return super.reTry( () -> super.toJavaResult( () -> stub().removeFromPersonalFeed(user, mid, pwd)));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {

        return super.reTry( () -> super.toJavaResult( () -> stub().getMessage(user, mid)));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {

        return super.reTry( () -> super.toJavaResult( () -> stub().getMessages(user, time)));
    }

    @Override
    public Result<List<Message>> getMessagesFromRemote(String user, String originalDomain, long time) {
        return super.reTry( () -> super.toJavaResult( () -> stub().getMessagesFromRemote(user, originalDomain, time)));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {

        return super.reTry( () -> super.toJavaResult( () -> stub().subUser(user, userSub, pwd)));
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {

        return super.reTry( () -> super.toJavaResult( () -> stub().unsubscribeUser(user, userSub, pwd)));
    }

    @Override
    public Result<List<String>> listSubs(String user) {

        return super.reTry( () -> super.toJavaResult( () -> stub().listSubs(user)));
    }

    @Override
    public Result<Void> deleteFeed(String user) {
        return super.reTry( () -> super.toJavaResult( () -> stub().deleteFeed(user)));
    }
}
