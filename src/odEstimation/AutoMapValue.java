/**
 * 
 */
package odEstimation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Unknown
 *
 */
public class AutoMapValue<K, V>
{
	private Map<K, AutoMapValue<K, V>> map;
	private V value;
	
	public Map<K, AutoMapValue<K, V>> getMap()
	{
		if (map == null) map = new HashMap<K, AutoMapValue<K, V>>();
		return map;
	}
	
	public AutoMapValue<K, V> get(K key)
	{
		Map<K, AutoMapValue<K, V>> map = getMap();
		AutoMapValue<K, V> e = map.get(key);
		if (e == null) map.put(key, e = new AutoMapValue<K, V>());
		return e;
	}
	
	public AutoMapValue<K, V> get(K... keys)
	{
		AutoMapValue<K, V> e = this;
		for(K k : keys) e = e.get(k);
		return e;
	}
	
	public V get() {
		return value;
	}
	
	public void set(V value) {
		this.value = value;
	}
	
	public String toString() {
		return map + "|" + value;
	}
}
