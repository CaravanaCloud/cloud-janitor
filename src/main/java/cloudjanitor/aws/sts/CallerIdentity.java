package cloudjanitor.aws.sts;

import java.util.Optional;

public record CallerIdentity(
        String accountId,
        Optional<String> accountAlias) {
    public String getAccountName(){
        return accountAlias.orElse(accountId);
    }
}
