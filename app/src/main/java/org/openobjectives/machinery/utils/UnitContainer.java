/**
 * License APACHE-2.0  https://www.apache.org/licenses/LICENSE-2.0.html
 */
package org.openobjectives.machinery.utils;

import org.openobjectives.machinery.model.Unit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <B>Class: UnitContainer </B> <br>
 * <B>Class changed: </B> 19.05.2020 <br>
 * **************************************************************************** <br>
 * Class description: The cluster abstraction to be saved in db <br>
 * <br>
 * ***************************************************************************** <br>
 *
 */
public class UnitContainer implements Serializable, Cloneable {

	private static final long serialVersionUID = Double.doubleToLongBits(1.4);
	private static final String TAG = UnitContainer.class.getSimpleName();
	private AtomicLong nextID = new AtomicLong(0);

	private List<Unit> units = new ArrayList<Unit>();

	void saveUnit(Unit newunit) {
		if (0 == newunit.getId()) {
			newunit.setId(nextID.incrementAndGet());
			units.add(newunit);
		} else {
			List<Unit> tmp = new ArrayList<Unit>();
			for (int i = 0; i < units.size(); i++) {
				Unit u = units.get(i);
				if (u.getId() == newunit.getId()) {
					tmp.add(i, newunit);
				} else {
					tmp.add(i, u);
				}
			}
			units = tmp;
		}

	}

	boolean removeUnit(Unit unit) {
		return units.remove(unit);
	}

	List<Unit> getAllUnits() {
		return Collections.unmodifiableList(units);
	}

	Unit findUnit(long id) {
		for (Unit u : units) {
			if (u.getId() == id) {
				return u;
			}
		}
		return null;
	}

	void clear() {
		units.clear();
		nextID.set(1);
	}

}
