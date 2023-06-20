import IAnalyticExecution from '../interface/IAnalyticExecution';
class TestEvent implements IAnalyticExecution {
  public execute() {
        console.log('execute from test event');
    }
}

export default TestEvent;
