package Base;

public class Token {
    private TokenOfCS tokenOfCS;
    private byte[] tokenOfDataServer;

    public Token(TokenOfCS tokenOfCS, byte[] tokenOfDataServer) {
        this.tokenOfCS = tokenOfCS;
        this.tokenOfDataServer = tokenOfDataServer;
    }

    public TokenOfCS getTokenOfCS() {
        return tokenOfCS;
    }

    public void setTokenOfCS(TokenOfCS tokenOfCS) {
        this.tokenOfCS = tokenOfCS;
    }

    public byte[] getTokenOfDataServer() {
        return tokenOfDataServer;
    }

    public void setTokenOfDataServer(byte[] tokenOfDataServer) {
        this.tokenOfDataServer = tokenOfDataServer;
    }
}
