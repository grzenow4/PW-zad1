package cp2022.solution;

import cp2022.base.WorkplaceId;

import java.util.*;

public class SwitchMaps {
    private final HashMap<WorkplaceId, WorkplaceId> edgeOut;
    private final HashMap<WorkplaceId, Deque<WorkplaceId>> edgesIn;
    private final BiMap workshopInfo;

    public SwitchMaps(BiMap workshopInfo) {
        edgeOut = new HashMap<>();
        edgesIn = new HashMap<>();
        this.workshopInfo = workshopInfo;
    }

    public void addEdge(WorkplaceId oldId, WorkplaceId newId) {
        edgeOut.put(oldId, newId);
        if (edgesIn.get(newId) == null) {
            edgesIn.put(newId, new ArrayDeque<>());
        }
        edgesIn.get(newId).addLast(oldId);
    }

    public void removeEdge(WorkplaceId oldId, WorkplaceId newId) {
        edgeOut.remove(oldId);
        edgesIn.get(newId).remove(oldId);
    }

    public WorkplaceId getFirst(WorkplaceId wid) {
        if (edgesIn.get(wid) == null || edgesIn.get(wid).isEmpty()) {
            return null;
        }
        return edgesIn.get(wid).getFirst();
    }

    public ArrayList<WorkplaceId> checkCycle(WorkplaceId start) {
        ArrayList<WorkplaceId> res = new ArrayList<>();
        WorkplaceId cur = start;

        do {
            WorkplaceId next = edgeOut.get(cur);
            if (next == null) {
                return new ArrayList<>();
            }
            res.add(cur);
            cur = next;
        } while (cur != start);

        return res;
    }

    public void moveCycle(ArrayList<WorkplaceId> cycle) {
        for (int i = cycle.size() - 1; i > 0; i--) {
            WorkplaceId prev = cycle.get(i - 1);
            WorkplaceId cur = cycle.get(i);
            edgesIn.get(cur).remove(prev);
            edgesIn.get(cur).addFirst(prev);
        }
    }
}
