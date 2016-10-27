package io.subutai.common.peer;


/**
 * Exception thrown by peer methods
 */
public class PeerException extends Exception
{

    public PeerException( final Throwable cause )
    {
        super( cause );
    }


    public PeerException()
    {
    }


    public PeerException( final String message )
    {
        super( message );
    }


    public PeerException( final String message, final Throwable cause )
    {
        super( message, cause );
    }

    @Override
    public String toString()
    {
        return super.toString();
    }
}
