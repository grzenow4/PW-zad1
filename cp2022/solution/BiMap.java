package cp2022.solution;

import cp2022.base.WorkplaceId;

import java.util.HashMap;

public class BiMap {
    private final HashMap<Long, WorkplaceId> workerWorkplace;
    private final HashMap<WorkplaceId, Long> workplaceWorker;

    public BiMap() {
        workerWorkplace = new HashMap<>();
        workplaceWorker = new HashMap<>();
    }

    public void insert(Long id, WorkplaceId wid) {
        workerWorkplace.put(id, wid);
        workplaceWorker.put(wid, id);
    }

    public void remove(Long id) {
        WorkplaceId wid = workerWorkplace.get(id);
        workerWorkplace.remove(id);
        workplaceWorker.remove(wid);
    }

    public WorkplaceId get(Long id) {
        return workerWorkplace.get(id);
    }

    public Long get(WorkplaceId wid) {
        return workplaceWorker.get(wid);
    }

    public boolean contains(Long id) {
        return workerWorkplace.containsKey(id);
    }

    public boolean contains(WorkplaceId wid) {
        return workplaceWorker.containsKey(wid);
    }
}
