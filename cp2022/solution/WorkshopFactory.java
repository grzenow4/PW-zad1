/*
 * University of Warsaw
 * Concurrent Programming Course 2022/2023
 * Java Assignment
 *
 * Author: Konrad Iwanicki (iwanicki@mimuw.edu.pl)
 */
package cp2022.solution;

import cp2022.base.Workplace;
import cp2022.base.WorkplaceId;
import cp2022.base.Workshop;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public final class WorkshopFactory {
    private static HashMap<WorkplaceId, Semaphore> initSemaphores(Collection<Workplace> workplaces) {
        HashMap<WorkplaceId, Semaphore> res = new HashMap<>();

        for (Workplace workplace : workplaces) {
            res.put(workplace.getId(), new Semaphore(0, true));
        }

        return res;
    }

    private static HashMap<WorkplaceId, Integer> initEnters(Collection<Workplace> workplaces) {
        HashMap<WorkplaceId, Integer> res = new HashMap<>();

        for (Workplace workplace : workplaces) {
            res.put(workplace.getId(), 0);
        }

        return res;
    }

    private static WorkplaceDecorator getWorkplace(Collection<WorkplaceDecorator> workplaces, WorkplaceId wid) {
        for (WorkplaceDecorator workplace : workplaces) {
            if (workplace.getId() == wid) {
                return workplace;
            }
        }
        return null;
    }

    public final static Workshop newWorkshop(Collection<Workplace> workplaces) {
        int N = workplaces.size();
        Semaphore mutex = new Semaphore(1);

        Semaphore door = new Semaphore(2 * N, true);
        HashMap<WorkplaceId, Semaphore> queuesEnter = initSemaphores(workplaces);
        HashMap<WorkplaceId, Semaphore> queuesSwitch = initSemaphores(workplaces);
        HashMap<WorkplaceId, Integer> howManyEnters = initEnters(workplaces);

        BiMap workshopInfo = new BiMap();
        SwitchMaps switchMaps = new SwitchMaps(workshopInfo);

        Collection<WorkplaceDecorator> workplaceDecorators = workplaces.stream().map(
                WorkplaceDecorator::new
        ).collect(Collectors.toList());

        return new Workshop() {
            int workersCount = 0;

            @Override
            public Workplace enter(WorkplaceId wid) {
                long myId = Thread.currentThread().getId();
                WorkplaceDecorator wp = getWorkplace(workplaceDecorators, wid);
                assert wp != null;
                try {
                    door.acquire();
                    mutex.acquire();
                    workersCount++;
                    if (workshopInfo.contains(wid)) {
                        mutex.release();
                        howManyEnters.put(wid, howManyEnters.get(wid) + 1);
                        queuesEnter.get(wid).acquire();
                        howManyEnters.put(wid, howManyEnters.get(wid) - 1);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("panic: unexpected thread interruption");
                }
                workshopInfo.insert(myId, wid);
                mutex.release();
                return wp;
            }

            @Override
            public Workplace switchTo(WorkplaceId wid) {
                long myId = Thread.currentThread().getId();
                WorkplaceDecorator wp = getWorkplace(workplaceDecorators, wid);
                assert wp != null;
                try {
                    mutex.acquire();
                    WorkplaceId oldWid = workshopInfo.get(myId);

                    if (oldWid == wid) {
                        mutex.release();
                        return wp;
                    }

                    if (workshopInfo.contains(wid)) {
                        switchMaps.addEdge(oldWid, wid);
                        ArrayList<WorkplaceId> cycle = switchMaps.checkCycle(oldWid);
                        if (cycle.isEmpty()) {
                            mutex.release();
                            queuesSwitch.get(oldWid).acquire();
                        } else {
                            CountDownLatch latch = new CountDownLatch(cycle.size());
                            for (WorkplaceId w: cycle) {
                                assert getWorkplace(workplaceDecorators, w) != null;
                                getWorkplace(workplaceDecorators, w).setLatch(latch);
                            }
                            switchMaps.moveCycle(cycle);
                        }
                        switchMaps.removeEdge(oldWid, wid);
                    }

                    WorkplaceId next = switchMaps.getFirst(oldWid);
                    workshopInfo.remove(myId);
                    workshopInfo.insert(myId, wid);
                    if (next != null) {
                        queuesSwitch.get(next).release();
                    } else if (howManyEnters.get(oldWid) > 0) {
                        queuesEnter.get(oldWid).release();
                    } else {
                        mutex.release();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("panic: unexpected thread interruption");
                }
                return wp;
            }

            @Override
            public void leave() {
                long myId = Thread.currentThread().getId();
                try {
                    mutex.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException("panic: unexpected thread interruption");
                }
                workersCount--;
                WorkplaceId wid = workshopInfo.get(myId);
                workshopInfo.remove(myId);
                WorkplaceId next = switchMaps.getFirst(wid);

                if (next != null) {
                    queuesSwitch.get(next).release();
                } else if (howManyEnters.get(wid) > 0) {
                    queuesEnter.get(wid).release();
                } else if (workersCount == 0) {
                    int freeSpace = door.availablePermits();
                    door.release(2 * N - freeSpace);
                    mutex.release();
                } else {
                    mutex.release();
                }
            }
        };
    }
}
