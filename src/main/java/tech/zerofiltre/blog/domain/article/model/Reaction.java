package tech.zerofiltre.blog.domain.article.model;


public class Reaction {

    private long id;
    private Action action;
    private long authorId;
    private long articleId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public long getArticleId() {
        return articleId;
    }

    public void setArticleId(long articleId) {
        this.articleId = articleId;
    }


    public enum Action {
        LOVE,
        FIRE,
        LIKE,
        CLAP
    }


}

