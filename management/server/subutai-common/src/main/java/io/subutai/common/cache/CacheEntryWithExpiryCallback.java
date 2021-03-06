/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.subutai.common.cache;


import com.google.common.base.Preconditions;


/**
 * This class represents entry for {@code ExpiringCache}. Holds generic value for the specified ttl. When entry is
 * expired the supplied ExpiryCallback is called.
 */
class CacheEntryWithExpiryCallback<V> extends CacheEntry<V>
{

    /**
     * callback to trigger when entry expires
     */
    private final EntryExpiryCallback<V> expiryCallback;


    /**
     * Initializes the {@code CacheEntryWithExpiryCallback}
     *
     * @param value - entry value
     * @param ttlMs - entry ttl in milliseconds
     * @param expiryCallback -- callback to trigger when entry expires
     */
    CacheEntryWithExpiryCallback( V value, long ttlMs, EntryExpiryCallback<V> expiryCallback )
    {
        super( value, ttlMs );
        Preconditions.checkNotNull( expiryCallback, "Callback is null" );
        this.expiryCallback = expiryCallback;
    }


    /**
     * triggers the entry expiry callback
     */
    void callExpiryCallback()
    {
        expiryCallback.onEntryExpiry( super.getValue() );
    }
}
